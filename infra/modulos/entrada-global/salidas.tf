output "bucket_web" {
  description = "Bucket del frontend"
  value       = aws_s3_bucket.web.bucket
}

output "distribucion_id" {
  description = "Distribución de CloudFront"
  value       = aws_cloudfront_distribution.web.id
}

output "url_web" {
  description = "Dirección pública del frontend"
  value       = "https://${aws_cloudfront_distribution.web.domain_name}"
}
