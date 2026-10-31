output "alb_dominio" {
  description = "Dirección del ALB"
  value       = aws_lb.api.dns_name
}

output "repositorio_url" {
  description = "Repositorio de la imagen"
  value       = aws_ecr_repository.api.repository_url
}

output "grupo_autoescalado" {
  description = "Grupo de EC2"
  value       = aws_autoscaling_group.api.name
}
