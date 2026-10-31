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
