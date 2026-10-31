data "aws_iam_policy_document" "confianza" {
  statement {
    actions = ["sts:AssumeRole"]

    principals {
      type        = "Service"
      identifiers = ["lambda.amazonaws.com"]
    }
  }
}

resource "aws_iam_role" "funcion" {
  for_each = local.funciones

  name               = "${var.nombre}-${each.key}"
  assume_role_policy = data.aws_iam_policy_document.confianza.json
}

data "aws_iam_policy_document" "funcion" {
  for_each = local.funciones

  statement {
    sid       = "Registrar"
    actions   = ["logs:CreateLogStream", "logs:PutLogEvents"]
    resources = ["${aws_cloudwatch_log_group.funcion[each.key].arn}:*"]
  }

  statement {
    sid = "Consumir"
    actions = [
      "sqs:ChangeMessageVisibility",
      "sqs:DeleteMessage",
      "sqs:GetQueueAttributes",
      "sqs:ReceiveMessage",
    ]
    resources = [var.colas[each.value.cola]]
  }

  dynamic "statement" {
    for_each = each.key == "generar-boleto" ? [1] : []

    content {
      sid       = "GuardarBoletos"
      actions   = ["s3:PutObject"]
      resources = ["${var.bucket_boletos_arn}/boletos/*"]
    }
  }
}

resource "aws_iam_role_policy" "funcion" {
  for_each = local.funciones

  name   = "${var.nombre}-${each.key}"
  role   = aws_iam_role.funcion[each.key].id
  policy = data.aws_iam_policy_document.funcion[each.key].json
}
