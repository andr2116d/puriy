data "aws_caller_identity" "actual" {}

locals {
  nombre_rol_despliegue = "${var.prefijo}-github-despliegue-${var.ambiente}"
  arn_iam               = "arn:aws:iam::${data.aws_caller_identity.actual.account_id}"
}

# Con environment en el job, el sub lleva el ambiente y no la rama: por eso la rama va en :ref.
# Un PR sin integrar no entra: las reglas de rama del ambiente de GitHub no lo dejan usarlo
data "aws_iam_policy_document" "confianza_despliegue" {
  statement {
    actions = ["sts:AssumeRoleWithWebIdentity"]

    principals {
      type        = "Federated"
      identifiers = [aws_iam_openid_connect_provider.github.arn]
    }

    condition {
      test     = "StringEquals"
      variable = "token.actions.githubusercontent.com:aud"
      values   = ["sts.amazonaws.com"]
    }

    condition {
      test     = "StringEquals"
      variable = "token.actions.githubusercontent.com:sub"
      values   = ["repo:${var.repositorio_oidc}:environment:${var.ambiente}"]
    }

    condition {
      test     = "StringEquals"
      variable = "token.actions.githubusercontent.com:ref"
      values   = ["refs/heads/${var.rama_despliegue}"]
    }
  }
}

resource "aws_iam_role" "despliegue" {
  name                 = local.nombre_rol_despliegue
  description          = "Terraform y despliegues de GitHub Actions en ${var.ambiente}"
  assume_role_policy   = data.aws_iam_policy_document.confianza_despliegue.json
  max_session_duration = 3600
}

resource "aws_iam_role_policy_attachment" "despliegue_power_user" {
  role       = aws_iam_role.despliegue.name
  policy_arn = "arn:aws:iam::aws:policy/PowerUserAccess"
}

data "aws_iam_policy_document" "iam_del_proyecto" {
  statement {
    sid = "GestionarIamDelProyecto"
    actions = [
      "iam:AddRoleToInstanceProfile",
      "iam:AttachRolePolicy",
      "iam:CreateInstanceProfile",
      "iam:CreatePolicy",
      "iam:CreatePolicyVersion",
      "iam:CreateRole",
      "iam:DeleteInstanceProfile",
      "iam:DeletePolicy",
      "iam:DeletePolicyVersion",
      "iam:DeleteRole",
      "iam:DeleteRolePolicy",
      "iam:DetachRolePolicy",
      "iam:GetInstanceProfile",
      "iam:GetPolicy",
      "iam:GetPolicyVersion",
      "iam:GetRole",
      "iam:GetRolePolicy",
      "iam:ListAttachedRolePolicies",
      "iam:ListInstanceProfilesForRole",
      "iam:ListPolicyVersions",
      "iam:ListRolePolicies",
      "iam:PassRole",
      "iam:PutRolePolicy",
      "iam:RemoveRoleFromInstanceProfile",
      "iam:TagInstanceProfile",
      "iam:TagPolicy",
      "iam:TagRole",
      "iam:UntagRole",
      "iam:UpdateAssumeRolePolicy",
      "iam:UpdateRole",
    ]
    resources = [
      "${local.arn_iam}:role/${var.prefijo}-*",
      "${local.arn_iam}:policy/${var.prefijo}-*",
      "${local.arn_iam}:instance-profile/${var.prefijo}-*",
    ]
  }

  statement {
    sid       = "NoTocarseASiMismo"
    effect    = "Deny"
    actions   = ["iam:*"]
    resources = ["${local.arn_iam}:role/${local.nombre_rol_despliegue}"]
  }

  statement {
    sid       = "NoRepartirAdministrador"
    effect    = "Deny"
    actions   = ["iam:AttachRolePolicy", "iam:PutRolePermissionsBoundary"]
    resources = ["*"]

    condition {
      test     = "ArnEquals"
      variable = "iam:PolicyARN"
      values = [
        "arn:aws:iam::aws:policy/AdministratorAccess",
        "arn:aws:iam::aws:policy/IAMFullAccess",
      ]
    }
  }
}

resource "aws_iam_role_policy" "iam_del_proyecto" {
  name   = "${var.prefijo}-iam-del-proyecto"
  role   = aws_iam_role.despliegue.id
  policy = data.aws_iam_policy_document.iam_del_proyecto.json
}
