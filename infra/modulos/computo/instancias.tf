data "aws_region" "actual" {}

data "aws_iam_policy_document" "confianza" {
  statement {
    actions = ["sts:AssumeRole"]

    principals {
      type        = "Service"
      identifiers = ["ec2.amazonaws.com"]
    }
  }
}

resource "aws_iam_role" "api" {
  name               = "${var.nombre}-api"
  assume_role_policy = data.aws_iam_policy_document.confianza.json
}

resource "aws_iam_role_policy_attachment" "api" {
  for_each = toset([
    "arn:aws:iam::aws:policy/AmazonEC2ContainerRegistryReadOnly",
    "arn:aws:iam::aws:policy/AmazonSSMManagedInstanceCore",
  ])

  role       = aws_iam_role.api.name
  policy_arn = each.value
}

data "aws_iam_policy_document" "secreto" {
  statement {
    actions   = ["secretsmanager:GetSecretValue"]
    resources = [var.base.secreto_arn]
  }
}

resource "aws_iam_role_policy" "secreto" {
  name   = "${var.nombre}-api-secreto"
  role   = aws_iam_role.api.id
  policy = data.aws_iam_policy_document.secreto.json
}

resource "aws_iam_instance_profile" "api" {
  name = "${var.nombre}-api"
  role = aws_iam_role.api.name
}

resource "aws_security_group" "api" {
  name        = "${var.nombre}-api"
  description = "API desde el ALB"
  vpc_id      = var.vpc_id
}

resource "aws_vpc_security_group_ingress_rule" "api_alb" {
  security_group_id            = aws_security_group.api.id
  referenced_security_group_id = aws_security_group.alb.id
  ip_protocol                  = "tcp"
  from_port                    = 8080
  to_port                      = 8080
}

resource "aws_vpc_security_group_egress_rule" "api_salida" {
  security_group_id = aws_security_group.api.id
  cidr_ipv4         = "0.0.0.0/0"
  ip_protocol       = "-1"
}

resource "aws_vpc_security_group_ingress_rule" "base_api" {
  security_group_id            = var.base.grupo_seguridad_id
  referenced_security_group_id = aws_security_group.api.id
  ip_protocol                  = "tcp"
  from_port                    = 5432
  to_port                      = 5432
}

resource "aws_launch_template" "api" {
  name          = "${var.nombre}-api"
  image_id      = "resolve:ssm:/aws/service/ami-amazon-linux-latest/al2023-ami-kernel-default-arm64"
  instance_type = var.tipo_instancia

  user_data = base64encode(templatefile("${path.module}/arranque.sh.tftpl", {
    region      = data.aws_region.actual.region
    imagen      = "${aws_ecr_repository.api.repository_url}:actual"
    registro    = split("/", aws_ecr_repository.api.repository_url)[0]
    secreto_arn = var.base.secreto_arn
    direccion   = var.base.direccion
    base        = var.base.nombre
  }))

  iam_instance_profile {
    arn = aws_iam_instance_profile.api.arn
  }

  vpc_security_group_ids = [aws_security_group.api.id]

  metadata_options {
    http_endpoint               = "enabled"
    http_tokens                 = "required"
    http_put_response_hop_limit = 2
  }

  tag_specifications {
    resource_type = "instance"

    tags = {
      Name = "${var.nombre}-api"
    }
  }
}

resource "aws_autoscaling_group" "api" {
  name                      = "${var.nombre}-api"
  min_size                  = var.minimo_instancias
  max_size                  = var.maximo_instancias
  vpc_zone_identifier       = var.subredes_aplicacion
  target_group_arns         = [aws_lb_target_group.api.arn]
  health_check_type         = "ELB"
  health_check_grace_period = 300
  wait_for_capacity_timeout = "0"

  launch_template {
    id      = aws_launch_template.api.id
    version = aws_launch_template.api.latest_version
  }

  lifecycle {
    ignore_changes = [desired_capacity]
  }
}

resource "aws_autoscaling_policy" "cpu" {
  name                   = "${var.nombre}-api-cpu"
  autoscaling_group_name = aws_autoscaling_group.api.name
  policy_type            = "TargetTrackingScaling"

  target_tracking_configuration {
    target_value = var.cpu_objetivo

    predefined_metric_specification {
      predefined_metric_type = "ASGAverageCPUUtilization"
    }
  }
}
