resource "aws_wafv2_web_acl" "web" {
  provider = aws.global

  name  = "${var.nombre}-web"
  scope = "CLOUDFRONT"

  default_action {
    allow {}
  }

  rule {
    name     = "reglas-comunes"
    priority = 1

    override_action {
      none {}
    }

    statement {
      managed_rule_group_statement {
        name        = "AWSManagedRulesCommonRuleSet"
        vendor_name = "AWS"
      }
    }

    visibility_config {
      cloudwatch_metrics_enabled = true
      metric_name                = "${var.nombre}-reglas-comunes"
      sampled_requests_enabled   = true
    }
  }

  rule {
    name     = "limite-por-ip"
    priority = 2

    action {
      block {}
    }

    statement {
      rate_based_statement {
        limit              = var.limite_por_ip
        aggregate_key_type = "IP"
      }
    }

    visibility_config {
      cloudwatch_metrics_enabled = true
      metric_name                = "${var.nombre}-limite-por-ip"
      sampled_requests_enabled   = true
    }
  }

  visibility_config {
    cloudwatch_metrics_enabled = true
    metric_name                = "${var.nombre}-web"
    sampled_requests_enabled   = true
  }
}
