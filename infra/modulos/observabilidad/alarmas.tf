resource "aws_cloudwatch_metric_alarm" "cola_de_errores" {
  for_each = var.colas_de_errores

  alarm_name          = "${var.nombre}-dlq-${each.key}"
  alarm_description   = "Hay mensajes en la DLQ de ${each.key}"
  namespace           = "AWS/SQS"
  metric_name         = "ApproximateNumberOfMessagesVisible"
  dimensions          = { QueueName = each.value }
  statistic           = "Maximum"
  period              = 300
  evaluation_periods  = 1
  threshold           = 0
  comparison_operator = "GreaterThanThreshold"
  treat_missing_data  = "notBreaching"
}
