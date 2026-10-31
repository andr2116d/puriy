variable "nombre" {
  description = "Nombre base de los recursos"
  type        = string
}

variable "ruta_paquetes" {
  description = "Carpeta de los proyectos de las Lambdas"
  type        = string
}

variable "colas" {
  description = "ARN de la cola de cada consumo"
  type        = map(string)
}

variable "bucket_boletos_arn" {
  description = "ARN del bucket de boletos"
  type        = string
}

variable "memoria_mb" {
  description = "Memoria de cada Lambda"
  type        = number
  default     = 512
}

variable "tiempo_limite_segundos" {
  description = "Tiempo máximo de cada ejecución"
  type        = number
  default     = 30
}
