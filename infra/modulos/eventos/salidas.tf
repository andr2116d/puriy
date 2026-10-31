output "colas" {
  description = "ARN de las colas que consumen las Lambdas"
  value = merge(
    { for nombre, cola in aws_sqs_queue.fifo : nombre => cola.arn },
    { notificaciones = aws_sqs_queue.notificaciones.arn },
  )
}

output "bucket_boletos_arn" {
  description = "ARN del bucket de boletos"
  value       = aws_s3_bucket.boletos.arn
}

output "colas_de_errores" {
  description = "Nombre de cada DLQ"
  value = merge(
    { for nombre, cola in aws_sqs_queue.fifo_dlq : nombre => cola.name },
    { notificaciones = aws_sqs_queue.notificaciones_dlq.name },
  )
}
