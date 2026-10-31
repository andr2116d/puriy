variable "region" {
  description = "Región de AWS"
  type        = string
  default     = "us-east-2"
}

variable "prefijo" {
  description = "Prefijo de los recursos"
  type        = string
  default     = "puriy-project"
}

variable "ambiente" {
  description = "Ambiente desplegado"
  type        = string
  default     = "dev"
}
