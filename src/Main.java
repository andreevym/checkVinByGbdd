import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;

public class Main {

    private static final String USER_AGENT = "Mozilla/5.0 (iPad; CPU OS 9_1 like Mac OS X) AppleWebKit/601.1.46 (KHTML, like Gecko) Version/9.0 Mobile/13B143 Safari/601.1";
    private static final String JSESSIONID = "JSESSIONID";

    public static void main(String[] args) throws IOException {
        // String jsessionid = saveImage("new.jpg");

        String vin = "11111111111111111111";
        String captchaWord = "94746";
        String sessionId = "35493B526CCFED4B8578BCE3593F02B2";

        clientRequest(vin, captchaWord, CheckType.HISTORY, sessionId);
        clientRequest(vin, captchaWord, CheckType.WANTED, sessionId);
        clientRequest(vin, captchaWord, CheckType.RESTRICT, sessionId);
        clientRequest(vin, captchaWord, CheckType.DTP, sessionId);

        //
        // history
        //{"message":"Прошло слишком много времени с момента загрузки картинки, повторите попытку","status":201}
        //{"RequestResult":null,"vin":"11111111111111111111","regnum":null,"message":"404:No data found","status":404}

        // http://check.gibdd.ru/proxy/check/auto/wanted
        // checkType:wanted
        //{"RequestResult":{"records":[],"count":1,"error":0},"vin":"11111111111111111111","status":200}


        // restrict
        // {"RequestResult":{"records":[],"count":0,"error":0},"vin":"11111111111111111111","status":200}

        // DTP
        // {"RequestResult":{"errorDescription":"","statusCode":1,"Accidents":[]},"vin":"11111111111111111111","status":200}
    }

    private static String getSessionId() throws IOException {
        URL url = new URL("http://www.gibdd.ru/check/auto/");
        HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();
        urlConnection.setRequestMethod("GET");
        urlConnection.setRequestProperty("User-Agent", USER_AGENT);

        writeResponseToHtml(urlConnection, false);

        return getSessionIdByURLConnection(urlConnection);
    }

    private static String getSessionIdByURLConnection(URLConnection urlConnection) {
        Map<String, List<String>> headerFields = urlConnection.getHeaderFields();

        System.out.println("find session id in headerFields");
        for (Map.Entry<String, List<String>> next : headerFields.entrySet()) {
            String key = next.getKey();
            if (isCookie(key)) {
                return findSessionId(next.getValue());
            }
        }

        throw new NotFoundResult(String.format("can't find %s id", JSESSIONID));
    }

    private static String findSessionId(List<String> value) {
        for (String s : value) {
            if (s.startsWith(JSESSIONID)) {
                int start = s.indexOf("=") + 1;
                int endIndex = s.indexOf(";");
                String result = s.substring(start, endIndex);
                System.out.println(JSESSIONID + ": " + result);
                return result;
            }
        }
        throw new NotFoundResult(String.format("can't find %s id", JSESSIONID));
    }

    private static boolean isCookie(String key) {
        return key != null && (key.equals("Set-Cookie") || key.equals("Cookie"));
    }

    private static void printHeadersByUrlConnection(URLConnection urlConnection) {
        Map<String, List<String>> headerFields = urlConnection.getHeaderFields();
        for (Map.Entry<String, List<String>> next : headerFields.entrySet()) {
            String key = next.getKey();
            if (key != null && key.equals("Set-Cookie")) {
                List<String> value = next.getValue();
                System.out.println("key:" + key);
                System.out.println("value:" + value);
            }
        }
    }

    private static void clientRequest(String vin, String captchaWord, CheckType checkType, String jsessionid) throws IOException {
        String client = String.format("http://check.gibdd.ru/proxy/check/auto/%s", checkType.getValue());
        URL url = new URL(client);
        HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();
        urlConnection.setRequestMethod("POST");
        urlConnection.setRequestProperty("User-Agent", USER_AGENT);
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

    private static String saveImage(String destinationFile) throws IOException {
        URL url = new URL("http://check.gibdd.ru/proxy/captcha.jpg");
        HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();
        urlConnection.setRequestMethod("GET");
        urlConnection.setRequestProperty("User-Agent", USER_AGENT);
        urlConnection.setRequestProperty("X-Compress", "0");
        urlConnection.setRequestProperty("Referer", "http://www.gibdd.ru/check/auto/");
        urlConnection.setRequestProperty("Host", "www.gibdd.ru");
        urlConnection.setRequestProperty("Accept", "image/webp,image/*,*/*;q=0.8");
        urlConnection.setRequestProperty("Accept-Encoding", "gzip, deflate, sdch");
        urlConnection.setRequestProperty("Accept-Language", "ru,en-US;q=0.8,en;q=0.6");
        urlConnection.setRequestProperty("Connection", "keep-alive");
        urlConnection.setRequestProperty("Cookie", "_ga=GA1.2.1593191729.1468750977; _ym_uid=1468750977525215266; _ym_isad=1");

        InputStream is = urlConnection.getInputStream();

        OutputStream os = new FileOutputStream(destinationFile);

        byte[] b = new byte[2048];
        int length;

        while ((length = is.read(b)) != -1) {
            os.write(b, 0, length);
        }

        is.close();
        os.close();

        printHeadersByUrlConnection(urlConnection);
        return getSessionIdByURLConnection(urlConnection);
    }

    private enum CheckType {
        HISTORY("history"),
        WANTED("wanted"),
        RESTRICT("restrict"),
        DTP("dtp");

        private final String value;

        CheckType(String value) {
            this.value = value;
        }

        public String getValue() {
            return value;
        }
    }
}