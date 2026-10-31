output "grupo_usuarios_id" {
  description = "Grupo de usuarios"
  value       = aws_cognito_user_pool.usuarios.id
}

output "cliente_web_id" {
  description = "Cliente del frontend"
  value       = aws_cognito_user_pool_client.web.id
}
