output "bucket_estado" {
  description = "Bucket del estado de Terraform"
  value       = aws_s3_bucket.estado.bucket
}
