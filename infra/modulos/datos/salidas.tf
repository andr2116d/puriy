output "direccion" {
  description = "Dirección de PostgreSQL"
  value       = aws_db_instance.base.address
}

output "base" {
  description = "Nombre de la base"
  value       = aws_db_instance.base.db_name
}

output "base_arn" {
  description = "ARN de la instancia"
  value       = aws_db_instance.base.arn
}

output "secreto_arn" {
  description = "Secreto con las credenciales"
  value       = aws_db_instance.base.master_user_secret[0].secret_arn
}

output "grupo_seguridad_id" {
  description = "Grupo de seguridad de la base"
  value       = aws_security_group.base.id
}
