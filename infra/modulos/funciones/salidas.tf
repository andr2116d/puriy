output "funciones" {
  description = "Alias de cada Lambda"
  value       = [for alias in aws_lambda_alias.vivo : alias.arn]
}
