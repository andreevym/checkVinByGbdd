import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;

/**
 * Данный проект эммулирует взаимодействие пользователя с сайтом гибдд на странице "ПРОВЕРКА ТРАНСПОРТНОГО СРЕДСТВА".
 */
public class AutoInfoService {

    public static void main(String[] args) throws IOException {
        String vin = "11111111111111111111"; // Укажите VIN
        String captchaWord = "94746"; // открыть капчу (new.jpg), прочитать номер и ввести в поле captchaWord
        String sessionId = "35493B526CCFED4B8578BCE3593F02B2"; // jsessionid полученный вместе с капчей
        CheckType checkType = CheckType.HISTORY; // Выбрать один из типов запросов
        /*
            HISTORY("history"), // Проверка истории регистрации в ГИБДД
            WANTED("wanted"), // Проверка нахождения в розыске
            RESTRICT("restrict"), // Проверка наличия ограничений
            DTP("dtp"); // Проверка на участие в дорожно-транспортных происшествиях
        */

        clientRequest(vin, captchaWord, checkType, sessionId);

        // checkType: history
        //{"message":"Прошло слишком много времени с момента загрузки картинки, повторите попытку","status":201}
        //{"RequestResult":null,"vin":"11111111111111111111","regnum":null,"message":"404:No data found","status":404}

        // checkType:wanted
        //{"RequestResult":{"records":[],"count":1,"error":0},"vin":"11111111111111111111","status":200}

        // restrict
        // {"RequestResult":{"records":[],"count":0,"error":0},"vin":"11111111111111111111","status":200}

        // dtp
        // {"RequestResult":{"errorDescription":"","statusCode":1,"Accidents":[]},"vin":"11111111111111111111","status":200}
    }

    private static void clientRequest(String vin, String captchaWord, CheckType checkType, String jsessionid) throws IOException {
        String client = String.format("http://check.gibdd.ru/proxy/check/auto/%s", checkType.getValue());
        URL url = new URL(client);
        HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();
        urlConnection.setRequestMethod("POST");
        urlConnection.setRequestProperty("User-Agent", CommonRequest.USER_AGENT);
        urlConnection.setRequestProperty("X-Compress", "0");
        urlConnection.setRequestProperty("Referer", "http://www.gibdd.ru/check/auto/");
        urlConnection.setRequestProperty("Host", "www.gibdd.ru");
        urlConnection.setRequestProperty("Accept", "image/webp,image/*,*/*;q=0.8");
        urlConnection.setRequestProperty("Accept-Encoding", "gzip, deflate, sdch");
        urlConnection.setRequestProperty("Accept-Language", "ru,en-US;q=0.8,en;q=0.6");
        urlConnection.setRequestProperty("Connection", "keep-alive");
        String cookie = String.format("JSESSIONID=%s; _ga=GA1.2.1593191729.1468750977; _ym_uid=1468750977525215266; _ym_isad=1", jsessionid);
        urlConnection.setRequestProperty("Cookie", cookie);

        // Send post request
        urlConnection.setDoOutput(true);
        DataOutputStream wr = new DataOutputStream(urlConnection.getOutputStream());
        String urlParameters = String.format("vin=%s&captchaWord=%s&checkType=%s", vin, captchaWord, checkType);
        wr.writeBytes(urlParameters);
        wr.flush();
        wr.close();


        int responseCode = urlConnection.getResponseCode();
        System.out.println("\nSending 'POST' request to URL : " + url);
        System.out.println("Post parameters : " + urlParameters);
        System.out.println("Cookie : " + cookie);
        System.out.println("Response Code : " + responseCode);
        writeResponseToHtml(urlConnection, true);

    }

    private static void writeResponseToHtml(HttpURLConnection urlConnection, boolean isPrintResponse) throws IOException {
        BufferedReader in = new BufferedReader(new InputStreamReader(urlConnection.getInputStream()));

        String inputLine;
        StringBuilder response = new StringBuilder();

        while ((inputLine = in.readLine()) != null) {
            response.append(inputLine);
        }
        in.close();

        File file = new File("index.html");
        if (!file.exists()) {
            boolean newFile = file.createNewFile();
            if (newFile) {
                System.out.println("file was created: " + file.getAbsolutePath());
            }
        }
        Path path = file.toPath();
        try (BufferedWriter writer = Files.newBufferedWriter(path)) {
            writer.write(response.toString());
        }

        if (isPrintResponse) {
            System.out.println(response.toString());
        }
    }
}