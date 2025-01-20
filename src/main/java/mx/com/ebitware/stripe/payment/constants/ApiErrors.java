package mx.com.ebitware.stripe.payment.constants;

import mx.com.ebitware.stripe.payment.exception.ApiError;
import org.springframework.http.HttpStatus;

public class ApiErrors {

    public static final ApiError INTERNAL_SERVER_ERROR(String language) {
        ApiError error = null;
        switch (language) {
            case "en":
                error = new ApiError(
                        HttpStatus.INTERNAL_SERVER_ERROR,
                        1000,
                        "Unexpected error ocurred with the server",
                        "Unexpected error ocurred with the server"
                );
                break;
            case "pt":
                error = new ApiError(
                        HttpStatus.INTERNAL_SERVER_ERROR,
                        1000,
                        "Erro inesperado ocorrido com o servidor",
                        "Erro inesperado ocorrido com o servidor"
                );
                break;
            default:
                error = new ApiError(
                        HttpStatus.INTERNAL_SERVER_ERROR,
                        1000,
                        "Ocurrio un error inesperado con el servidor",
                        "Unexpected error ocurred with the server"
                );
                break;
        }
        return error;
    }

    ;

    public static final ApiError FILE_NOT_FOUND(String language) {
        ApiError error = null;
        switch (language) {
            case "en":
                error = new ApiError(
                        HttpStatus.NOT_FOUND,
                        1000,
                        "File not found in server",
                        "File not found in server"
                );
                break;
            case "pt":
                error = new ApiError(
                        HttpStatus.NOT_FOUND,
                        1000,
                        "Ficheiro não encontrado no servidor",
                        "Ficheiro não encontrado no servidor"
                );
                break;
            default:
                error = new ApiError(
                        HttpStatus.NOT_FOUND,
                        1000,
                        "No se encontró el archivo",
                        "No se encontró el archivo"
                );
                break;
        }
        return error;
    }

    public static final ApiError FILE_CANT_STORED(String language) {
        ApiError error = null;
        switch (language) {
            case "en":
                error = new ApiError(
                        HttpStatus.CONFLICT,
                        1001,
                        "File cannot be stored",
                        "File cannot be stored"
                );
                break;
            case "pt":
                error = new ApiError(
                        HttpStatus.CONFLICT,
                        1001,
                        "Ficheiro não encontrado no servidor",
                        "Ficheiro não encontrado no servidor"
                );
                break;
            default:
                error = new ApiError(
                        HttpStatus.CONFLICT,
                        1001,
                        "No se puede cargar el archivo",
                        "No se puede cargar el archivo"
                );
                break;
        }
        return error;
    }

    public static final ApiError FILE_CANT_CREATE_DIRECTORY(String language) {
        ApiError error = null;
        switch (language) {
            case "en":
                error = new ApiError(
                        HttpStatus.CONFLICT,
                        1012,
                        "File cannot be stored",
                        "File cannot be stored"
                );
                break;
            case "pt":
                error = new ApiError(
                        HttpStatus.CONFLICT,
                        1012,
                        "Ficheiro não encontrado no servidor",
                        "Ficheiro não encontrado no servidor"
                );
                break;
            default:
                error = new ApiError(
                        HttpStatus.CONFLICT,
                        1012,
                        "No se puede cargar el archivo",
                        "No se puede cargar el archivo"
                );
                break;
        }
        return error;
    }

    public static final ApiError FILE_INVALID_PATH(String language) {
        ApiError error = null;
        switch (language) {
            case "en":
                error = new ApiError(
                        HttpStatus.CONFLICT,
                        1013,
                        "File cannot be stored",
                        "File cannot be stored"
                );
                break;
            case "pt":
                error = new ApiError(
                        HttpStatus.CONFLICT,
                        1013,
                        "Ficheiro não encontrado no servidor",
                        "Ficheiro não encontrado no servidor"
                );
                break;
            default:
                error = new ApiError(
                        HttpStatus.CONFLICT,
                        1013,
                        "No se puede cargar el archivo",
                        "No se puede cargar el archivo"
                );
                break;
        }
        return error;
    }

    public static final ApiError FILE_INVALID_SIZE(String language) {
        ApiError error = null;
        switch (language) {
            case "en":
                error = new ApiError(
                        HttpStatus.CONFLICT,
                        1014,
                        "Maximum size of 5MB",
                        "Maximum size of 5MB"
                );
                break;
            case "pt":
                error = new ApiError(
                        HttpStatus.CONFLICT,
                        1014,
                        "Tamanho máximo de 5MB",
                        "Tamanho máximo de 5MB"
                );
                break;
            default:
                error = new ApiError(
                        HttpStatus.CONFLICT,
                        1014,
                        "Tamaño maximo de 5MB",
                        "Tamaño maximo de 5MB"
                );
                break;
        }
        return error;
    }

    public static final ApiError CONTACT_NOT_FOUND(String language) {
        ApiError error = null;
        switch (language) {
            case "en":
                error = new ApiError(
                        HttpStatus.NOT_FOUND,
                        2000,
                        "Contact does not exist",
                        "Contact does not exist"
                );
                break;
            case "pt":
                error = new ApiError(
                        HttpStatus.NOT_FOUND,
                        2000,
                        "O contacto não existe",
                        "O contacto não existe"
                );
                break;
            default:
                error = new ApiError(
                        HttpStatus.NOT_FOUND,
                        2000,
                        "El contacto no existe",
                        "El contacto no existe"
                );
                break;
        }
        return error;
    }

    public static final ApiError CONTACT_EXISTS(String language) {
        ApiError error = null;
        switch (language) {
            case "en":
                error = new ApiError(
                        HttpStatus.CONFLICT,
                        2001,
                        "The contact was already registered previously",
                        "The contact was already registered previously"
                );
                break;
            case "pt":
                error = new ApiError(
                        HttpStatus.CONFLICT,
                        2001,
                        "O contacto já estava registado anteriormente",
                        "O contacto já estava registado anteriormente"
                );
                break;
            default:
                error = new ApiError(
                        HttpStatus.CONFLICT,
                        2001,
                        "El contacto ya estaba registrado anteriormente",
                        "El contacto ya estaba registrado anteriormente"
                );
                break;
        }
        return error;
    }

    public static final ApiError CONTACT_REGISTER_ERROR(String language) {
        ApiError error = null;
        switch (language) {
            case "en":
                error = new ApiError(
                        HttpStatus.BAD_GATEWAY,
                        2002,
                        "There was a problem registering the contact",
                        "There was a problem registering the contact"
                );
                break;
            case "pt":
                error = new ApiError(
                        HttpStatus.BAD_GATEWAY,
                        2002,
                        "Houve um problema em registar o contacto.",
                        "Houve um problema em registar o contacto."
                );
                break;
            default:
                error = new ApiError(
                        HttpStatus.BAD_GATEWAY,
                        2002,
                        "Hubo un problema al dar de alta el contacto",
                        "Hubo un problema al dar de alta el contacto"
                );
                break;
        }
        return error;
    }

    public static final ApiError CONTACT_DELETE_ERROR(String language) {
        ApiError error = null;
        switch (language) {
            case "en":
                error = new ApiError(
                        HttpStatus.BAD_GATEWAY,
                        2003,
                        "There was a problem deleting the contact",
                        "There was a problem deleting the contact"
                );
                break;
            case "pt":
                error = new ApiError(
                        HttpStatus.BAD_GATEWAY,
                        2003,
                        "Houve um problema em apagar o contacto",
                        "Houve um problema em apagar o contacto"
                );
                break;
            default:
                error = new ApiError(
                        HttpStatus.BAD_GATEWAY,
                        2003,
                        "Hubo un problema al eliminar el contacto",
                        "Hubo un problema al eliminar el contacto"
                );
                break;
        }
        return error;
    }

    public static final ApiError CONVERSATION_NOT_FOUND(String language) {
        ApiError error = null;
        switch (language) {
            case "en":
                error = new ApiError(
                        HttpStatus.NOT_FOUND,
                        2100,
                        "The conversation does not exist",
                        "The conversation does not exist"
                );
                break;
            case "pt":
                error = new ApiError(
                        HttpStatus.NOT_FOUND,
                        2100,
                        "A conversa não existe",
                        "A conversa não existe"
                );
                break;
            default:
                error = new ApiError(
                        HttpStatus.NOT_FOUND,
                        2100,
                        "La conversación no existe",
                        "La conversación no existe"
                );
                break;
        }
        return error;
    }

    public static final ApiError MESSAGE_SEND_ERROR(String language) {
        ApiError error = null;
        switch (language) {
            case "en":
                error = new ApiError(
                        HttpStatus.BAD_GATEWAY,
                        2200,
                        "There was a problem sending the message",
                        "There was a problem sending the message"
                );
                break;
            case "pt":
                error = new ApiError(
                        HttpStatus.BAD_GATEWAY,
                        2200,
                        "Havia um problema de envio da mensagem",
                        "Havia um problema de envio da mensagem"
                );
                break;
            default:
                error = new ApiError(
                        HttpStatus.BAD_GATEWAY,
                        2200,
                        "Hubo un problema al enviar el mensaje",
                        "Hubo un problema al enviar el mensaje"
                );
                break;
        }
        return error;
    }

    ////////////////////////////////////////////////////////////////

    public static final ApiError TOKEN_NOT_PROVIDED(String language) {
        ApiError error = null;
        switch (language) {
            case "en":
                error = new ApiError(
                        HttpStatus.UNAUTHORIZED,
                        1101,
                        "Token not provided",
                        "Token not provided"
                );
                break;
            case "pt":
                error = new ApiError(
                        HttpStatus.UNAUTHORIZED,
                        1101,
                        "Token não fornecido",
                        "Token não fornecido"
                );
                break;
            default:
                error = new ApiError(
                        HttpStatus.UNAUTHORIZED,
                        1101,
                        "Token no proporcionado",
                        "Token no proporcionado"
                );
                break;
        }
        return error;
    }

    public static final ApiError INVALID_TOKEN(String language) {
        ApiError error = null;
        switch (language) {
            case "en":
                error = new ApiError(
                        HttpStatus.UNAUTHORIZED,
                        1100,
                        "Invalid token",
                        "Invalid token"
                );
                break;
            case "pt":
                error = new ApiError(
                        HttpStatus.UNAUTHORIZED,
                        1100,
                        "Token inválido",
                        "Token inválido"
                );
                break;
            default:
                error = new ApiError(
                        HttpStatus.UNAUTHORIZED,
                        1100,
                        "Token inválido",
                        "Token inválido"
                );
                break;
        }
        return error;
    }

    public static final ApiError INVALID_COMPANY_ID(String language) {
        ApiError error = null;
        switch (language) {
            case "en":
                error = new ApiError(
                        HttpStatus.BAD_REQUEST,
                        1100,
                        "The logged in user does not belong to the company",
                        "The logged in user does not belong to the company"
                );
                break;
            case "pt":
                error = new ApiError(
                        HttpStatus.BAD_REQUEST,
                        1100,
                        "O utilizador com login não pertence à empresa",
                        "O utilizador com login não pertence à empresa"
                );
                break;
            default:
                error = new ApiError(
                        HttpStatus.BAD_REQUEST,
                        1100,
                        "El usuario que ha iniciado sesión no pertenece a la empresa",
                        "El usuario que ha iniciado sesión no pertenece a la empresa"
                );
                break;
        }
        return error;
    }

    //////////
    public static final ApiError CONVERSATION_OLDER_THAN_24_HRS(String language) {
        ApiError error = null;
        switch (language) {
            case "en":
                error = new ApiError(
                        HttpStatus.CONFLICT,
                        1403,
                        "Conversation is older than 24 hrs",
                        "Conversation is older than 24 hrs"
                );
                break;
            case "pt":
                error = new ApiError(
                        HttpStatus.CONFLICT,
                        1403,
                        "A conversa tem mais de 24 horas de duração",
                        "A conversa tem mais de 24 horas de duração"
                );
                break;
            default:
                error = new ApiError(
                        HttpStatus.CONFLICT,
                        1403,
                        "La conversación tiene más de 24 horas",
                        "La conversación tiene más de 24 horas"
                );
                break;
        }
        return error;
    }

    public static final ApiError NOT_SEND_TEMPLATE(String language, String phone) {
        ApiError error = null;
        switch (language) {
            case "en":
                error = new ApiError(
                        HttpStatus.CONFLICT,
                        1403,
                        "The template cannot be sent. The number entered (" + phone
                                + ") already has a conversation with status 'Template sent', 'On hold' or 'Attention'",
                        "The template cannot be sent. The number entered (" + phone
                                + ") already has a conversation with status 'Template sent', 'On hold' or 'Attention'"
                );
                break;
            case "pt":
                error = new ApiError(
                        HttpStatus.CONFLICT,
                        1403,
                        "O modelo não pode ser enviado. O número inserido (" + phone
                                + ") já tem uma conversa com o status 'Modelo enviado', 'Em espera' ou 'Atenção'",
                        "O modelo não pode ser enviado. O número inserido (" + phone
                                + ") já tem uma conversa com o status 'Modelo enviado', 'Em espera' ou 'Atenção'"
                );
                break;
            default:
                error = new ApiError(
                        HttpStatus.CONFLICT,
                        1403,
                        "El template no puede ser enviado. El número ingresado (" + phone
                                + ") ya cuenta con una conversación con estatus 'Template enviado', 'En espera' o 'En atención'",
                        "El template no puede ser enviado. El número ingresado (" + phone
                                + ") ya cuenta con una conversación con estatus 'Template enviado', 'En espera' o 'En atención'"
                );
                break;
        }
        return error;
    }

    public static final ApiError SEND_MESSAGE_ERROR(String language, String phone) {
        ApiError error = null;
        switch (language) {
            case "en":
                error = new ApiError(
                        HttpStatus.CONFLICT,
                        1403,
                        "Error sending message to number " + phone,
                        "Error sending message to number " + phone
                );
                break;
            case "pt":
                error = new ApiError(
                        HttpStatus.CONFLICT,
                        1403,
                        "Erro ao enviar mensagem para número " + phone,
                        "Erro ao enviar mensagem para número " + phone
                );
                break;
            default:
                error = new ApiError(
                        HttpStatus.CONFLICT,
                        1403,
                        "Error al enviar mensaje al número " + phone,
                        "Error al enviar mensaje al número " + phone
                );
                break;
        }
        return error;
    }

    public static final ApiError STATUS_INCORRECT(String language) {
        ApiError error = null;
        switch (language) {
            case "en":
                error = new ApiError(
                        HttpStatus.CONFLICT,
                        1403,
                        "Invalid status",
                        "Invalid status"
                );
                break;
            case "pt":
                error = new ApiError(
                        HttpStatus.CONFLICT,
                        1403,
                        "Estado inválido",
                        "Estado inválido"
                );
                break;
            default:
                error = new ApiError(
                        HttpStatus.CONFLICT,
                        1403,
                        "Estatus inválido",
                        "Estatus inválido"
                );
                break;
        }
        return error;
    }

}

