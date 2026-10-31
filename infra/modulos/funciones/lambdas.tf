locals {
  funciones = {
    generar-boleto = {
      manejador = "pe.puriy.funciones.generarboleto.GenerarBoletoFuncion::handleRequest"
      cola      = "boletos"
    }
    enviar-correo = {
      manejador = "pe.puriy.funciones.enviarcorreo.EnviarCorreoFuncion::handleRequest"
      cola      = "notificaciones"
    }
    publicar-asientos = {
      manejador = "pe.puriy.funciones.publicarasientos.PublicarAsientosFuncion::handleRequest"
      cola      = "asientos"
    }
  }
}

resource "aws_cloudwatch_log_group" "funcion" {
  for_each = local.funciones

  name              = "/aws/lambda/${var.nombre}-${each.key}"
  retention_in_days = 14
}

resource "aws_lambda_function" "funcion" {
  for_each = local.funciones

  function_name    = "${var.nombre}-${each.key}"
  role             = aws_iam_role.funcion[each.key].arn
  runtime          = "java25"
  architectures    = ["arm64"]
  handler          = each.value.manejador
  filename         = "${var.ruta_paquetes}/${each.key}/build/distributions/${each.key}.zip"
  source_code_hash = filebase64sha256("${var.ruta_paquetes}/${each.key}/build/distributions/${each.key}.zip")
  memory_size      = var.memoria_mb
  timeout          = var.tiempo_limite_segundos
  publish          = true

  snap_start {
    apply_on = "PublishedVersions"
  }

  depends_on = [
    aws_cloudwatch_log_group.funcion,
    aws_iam_role_policy.funcion,
  ]
}

resource "aws_lambda_alias" "vivo" {
  for_each = local.funciones

  name             = "vivo"
  function_name    = aws_lambda_function.funcion[each.key].arn
  function_version = aws_lambda_function.funcion[each.key].version
}

resource "aws_lambda_event_source_mapping" "cola" {
  for_each = local.funciones

  event_source_arn = var.colas[each.value.cola]
  function_name    = aws_lambda_alias.vivo[each.key].arn
  batch_size       = 1
}
