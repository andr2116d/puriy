output "colas" {
  description = "ARN de las colas que consumen las Lambdas"
  value       = { for nombre, cola in aws_sqs_queue.fifo : nombre => cola.arn }
}
