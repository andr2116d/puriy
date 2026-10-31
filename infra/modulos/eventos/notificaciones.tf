resource "aws_sqs_queue" "notificaciones_dlq" {
  name                      = "${var.nombre}-notificaciones-dlq"
  message_retention_seconds = 1209600
}

resource "aws_sqs_queue" "notificaciones" {
  name                       = "${var.nombre}-notificaciones"
  visibility_timeout_seconds = var.visibilidad_segundos

  redrive_policy = jsonencode({
    deadLetterTargetArn = aws_sqs_queue.notificaciones_dlq.arn
    maxReceiveCount     = var.maximo_recepciones
  })
}

resource "aws_cloudwatch_event_rule" "boleto_creado" {
  name        = "${var.nombre}-boleto-creado"
  description = "Boleto guardado en S3"

  event_pattern = jsonencode({
    source        = ["aws.s3"]
    "detail-type" = ["Object Created"]
    detail = {
      bucket = { name = [aws_s3_bucket.boletos.bucket] }
      object = { key = [{ prefix = "boletos/" }] }
    }
  })
}

resource "aws_cloudwatch_event_target" "notificaciones" {
  rule = aws_cloudwatch_event_rule.boleto_creado.name
  arn  = aws_sqs_queue.notificaciones.arn
}

data "aws_iam_policy_document" "notificaciones" {
  statement {
    actions   = ["sqs:SendMessage"]
    resources = [aws_sqs_queue.notificaciones.arn]

    principals {
      type        = "Service"
      identifiers = ["events.amazonaws.com"]
    }

    condition {
      test     = "ArnEquals"
      variable = "aws:SourceArn"
      values   = [aws_cloudwatch_event_rule.boleto_creado.arn]
    }
  }
}

resource "aws_sqs_queue_policy" "notificaciones" {
  queue_url = aws_sqs_queue.notificaciones.id
  policy    = data.aws_iam_policy_document.notificaciones.json
}
