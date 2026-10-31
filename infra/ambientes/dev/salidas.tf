output "bucket_web" {
  description = "Bucket del frontend"
  value       = module.entrada_global.bucket_web
}

output "distribucion_id" {
  description = "Distribución de CloudFront"
  value       = module.entrada_global.distribucion_id
}

output "url_web" {
  description = "Dirección pública del frontend"
  value       = module.entrada_global.url_web
}

output "funciones" {
  description = "Alias de cada Lambda"
  value       = module.funciones.funciones
}

output "repositorio_url" {
  description = "Repositorio de la imagen"
  value       = module.computo.repositorio_url
}

output "grupo_autoescalado" {
  description = "Grupo de EC2"
  value       = module.computo.grupo_autoescalado
}
