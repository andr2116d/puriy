locals {
  colas_fifo = {
    asientos = "AsientoActualizado"
    boletos  = "PagoConfirmado"
  }
}

resource "aws_sns_topic" "outbox" {
  name                        = "${var.nombre}-outbox.fifo"
  fifo_topic                  = true
  content_based_deduplication = true
}

resource "aws_sqs_queue" "fifo_dlq" {
  for_each = local.colas_fifo

  name                        = "${var.nombre}-${each.key}-dlq.fifo"
  fifo_queue                  = true
  content_based_deduplication = true
  message_retention_seconds   = 1209600
}

resource "aws_sqs_queue" "fifo" {
  for_each = local.colas_fifo

  name                        = "${var.nombre}-${each.key}.fifo"
  fifo_queue                  = true
  content_based_deduplication = true
  visibility_timeout_seconds  = var.visibilidad_segundos

  redrive_policy = jsonencode({
    deadLetterTargetArn = aws_sqs_queue.fifo_dlq[each.key].arn
    maxReceiveCount     = var.maximo_recepciones
  })
}

data "aws_iam_policy_document" "fifo" {
  for_each = local.colas_fifo

  statement {
    actions   = ["sqs:SendMessage"]
    resources = [aws_sqs_queue.fifo[each.key].arn]

    principals {
      type        = "Service"
      identifiers = ["sns.amazonaws.com"]
    }

    condition {
      test     = "ArnEquals"
      variable = "aws:SourceArn"
      values   = [aws_sns_topic.outbox.arn]
    }
  }
}

resource "aws_sqs_queue_policy" "fifo" {
  for_each = local.colas_fifo

  queue_url = aws_sqs_queue.fifo[each.key].id
  policy    = data.aws_iam_policy_document.fifo[each.key].json
}

resource "aws_sns_topic_subscription" "fifo" {
  for_each = local.colas_fifo

  topic_arn            = aws_sns_topic.outbox.arn
  protocol             = "sqs"
  endpoint             = aws_sqs_queue.fifo[each.key].arn
  raw_message_delivery = true
  filter_policy        = jsonencode({ tipo = [each.value] })

  depends_on = [aws_sqs_queue_policy.fifo]
}
