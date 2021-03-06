public enum CheckType {
    HISTORY("history"), // Проверка истории регистрации в ГИБДД
    WANTED("wanted"), // Проверка нахождения в розыске
    RESTRICT("restrict"), // Проверка наличия ограничений
    DTP("dtp"); // Проверка на участие в дорожно-транспортных происшествиях

    private final String value;

    CheckType(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }
}
