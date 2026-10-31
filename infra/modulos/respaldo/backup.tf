data "aws_iam_policy_document" "confianza" {
  statement {
    actions = ["sts:AssumeRole"]

    principals {
      type        = "Service"
      identifiers = ["backup.amazonaws.com"]
    }
  }
}

resource "aws_iam_role" "respaldo" {
  name               = "${var.nombre}-respaldo"
  assume_role_policy = data.aws_iam_policy_document.confianza.json
}

resource "aws_iam_role_policy_attachment" "respaldo" {
  role       = aws_iam_role.respaldo.name
  policy_arn = "arn:aws:iam::aws:policy/service-role/AWSBackupServiceRolePolicyForBackup"
}

resource "aws_backup_vault" "principal" {
  name          = var.nombre
  force_destroy = true
}

resource "aws_backup_plan" "base" {
  name = var.nombre

  rule {
    rule_name         = "diario"
    target_vault_name = aws_backup_vault.principal.name
    schedule          = "cron(0 8 * * ? *)"

    lifecycle {
      delete_after = 35
    }
  }

  rule {
    rule_name         = "semanal"
    target_vault_name = aws_backup_vault.principal.name
    schedule          = "cron(0 9 ? * SUN *)"

    lifecycle {
      delete_after = 180
    }
  }
}

resource "aws_backup_selection" "base" {
  name         = var.nombre
  plan_id      = aws_backup_plan.base.id
  iam_role_arn = aws_iam_role.respaldo.arn
  resources    = [var.base_arn]
}
