variable "region" {
  description = "Región de AWS donde se despliega todo"
  type        = string
}

variable "prefijo" {
  description = "Prefijo que identifica todos los recursos del proyecto"
  type        = string
}

variable "ambiente" {
  description = "Ambiente que se despliega"
  type        = string
}

variable "perfil_aws" {
  description = "Perfil local de IAM Identity Center"
  type        = string
}

variable "repositorio_oidc" {
  description = "Repositorio en el formato inmutable del sub de GitHub: dueño@id/repo@id"
  type        = string
}

variable "rama_despliegue" {
  description = "Única rama que puede desplegar este ambiente"
  type        = string
}
