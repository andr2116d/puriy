output "vpc_id" {
  description = "VPC"
  value       = aws_vpc.principal.id
}

output "subredes_publicas" {
  description = "Subredes públicas"
  value       = [for subred in aws_subnet.publica : subred.id]
}

output "subredes_aplicacion" {
  description = "Subredes de aplicación"
  value       = [for subred in aws_subnet.aplicacion : subred.id]
}

output "subredes_datos" {
  description = "Subredes de datos"
  value       = [for subred in aws_subnet.datos : subred.id]
}
