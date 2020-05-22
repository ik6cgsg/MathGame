package spbpu.hsamcp.mathgame.common

enum class AuthInfo(val str: String) {
    UUID("userUuid"),
    SERVER_ID("userServerId"),
    LOGIN("userLogin"),
    PASSWORD("userPassword"),
    NAME("userName"),
    SURNAME("userSurname"),
    SECOND_NAME("userSecondName"),
    GROUP("userGroup"),
    INSTITUTION("userInstitution"),
    AGE("userAge"),
    ADDITIONAL("userAdditional"),
    //STATISTICS("userStatistics"),
    AUTHORIZED("userAuthorized"),
    AUTH_STATUS("userAuthStatus"),
    TIME_COEFF("userTimeCoeff"),
    AWARD_COEFF("userAwardCoeff"),
    UNDO_COEFF("userUndoCoeff"),
    PREFIX("user")
}

class Storage {

}