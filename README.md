# check-auto-by-gbdd
Данный проект эммулирует взаимодействие пользователя с сайтом гибдд на странице "ПРОВЕРКА ТРАНСПОРТНОГО СРЕДСТВА".
Наше взаимодействие с сайтом делится на два этапа.
1) Получить капчу и jsessionid
CaptchaService.main
CaptchaResult captchaResult = captchaRequest("new.jpg");

2) Отправить запрос 
AutoInfoService.main
String vin = "11111111111111111111"; // Укажите VIN
String captchaWord = "94746"; // открыть капчу (new.jpg), прочитать номер и ввести в поле captchaWord
String sessionId = "35493B526CCFED4B8578BCE3593F02B2"; // jsessionid полученный вместе с капчей
CheckType checkType = CheckType.HISTORY; // Выбрать один из типов запросов
/*
enum CheckType 
HISTORY("history"), // Проверка истории регистрации в ГИБДД
WANTED("wanted"), // Проверка нахождения в розыске
RESTRICT("restrict"), // Проверка наличия ограничений
DTP("dtp"); // Проверка на участие в дорожно-транспортных происшествиях
*/

clientRequest(vin, captchaWord, checkType, sessionId);

В результате мы получаем следующий ответ:
checkType: history
{"message":"Прошло слишком много времени с момента загрузки картинки, повторите попытку","status":201}
{"RequestResult":null,"vin":"11111111111111111111","regnum":null,"message":"404:No data found","status":404}

checkType: wanted
{"RequestResult":{"records":[],"count":1,"error":0},"vin":"11111111111111111111","status":200}

checkType: restrict
{"RequestResult":{"records":[],"count":0,"error":0},"vin":"11111111111111111111","status":200}

checkType: dtp
{"RequestResult":{"errorDescription":"","statusCode":1,"Accidents":[]},"vin":"11111111111111111111","status":200}
