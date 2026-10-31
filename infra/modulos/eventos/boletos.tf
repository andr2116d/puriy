resource "aws_s3_bucket" "boletos" {
  bucket        = "${var.nombre}-boletos"
  force_destroy = true
}

resource "aws_s3_bucket_public_access_block" "boletos" {
  bucket = aws_s3_bucket.boletos.id

  block_public_acls       = true
  ignore_public_acls      = true
  block_public_policy     = true
  restrict_public_buckets = true
}

resource "aws_s3_bucket_notification" "boletos" {
  bucket      = aws_s3_bucket.boletos.id
  eventbridge = true
}
