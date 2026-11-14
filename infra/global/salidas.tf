output "bucket_estado" {
  description = "Bucket del estado de Terraform"
  value       = aws_s3_bucket.estado.bucket
}

output "proveedor_oidc_github" {
  description = "ARN del proveedor OIDC de GitHub"
  value       = aws_iam_openid_connect_provider.github.arn
}

output "rol_despliegue" {
  description = "Valor de la variable AWS_ROL_DESPLIEGUE del ambiente"
  value       = aws_iam_role.despliegue.arn
}
