import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;

public class Main {

    private static final String USER_AGENT = "Mozilla/5.0 (iPad; CPU OS 9_1 like Mac OS X) AppleWebKit/601.1.46 (KHTML, like Gecko) Version/9.0 Mobile/13B143 Safari/601.1";
    private static final String PHPSESS_ID = "PHPSESSID";

    public static void main(String[] args) throws IOException {
        /*String phpsessId = mainRequest();
        if (phpsessId == null) {
            System.out.println("ERROR");
            return;
        }

        saveImage(phpsessId, "new.jpg");*/

        clientRequest("11111111111111111111", "37072", "floevoi757tbh7fohp75l27f12");
    }

    private static String mainRequest() throws IOException {
        URL url = new URL("http://www.gibdd.ru/check/auto/");
        HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();
        urlConnection.setRequestMethod("GET");
        urlConnection.setRequestProperty("User-Agent", USER_AGENT);

        writeResponseToHtml(urlConnection, false);

        return getPhpsessIdByUrlConnection(urlConnection);
    }

    private static String getPhpsessIdByUrlConnection(URLConnection urlConnection) {
        String resultPhpsessId = null;
        Map<String, List<String>> headerFields = urlConnection.getHeaderFields();
        for (Map.Entry<String, List<String>> next : headerFields.entrySet()) {
            String key = next.getKey();
            List<String> value = next.getValue();
            if (key != null && key.equals("Set-Cookie")) {
                System.out.println("key: " + key);
                for (String s : value) {
                    if (s.startsWith(PHPSESS_ID)) {
                        int start = s.indexOf("=") + 1;
                        int endIndex = s.indexOf(";");
                        resultPhpsessId = s.substring(start, endIndex);
                    }
                }
            }
        }
        System.out.println("resultPhpsessId: " + resultPhpsessId);
        return resultPhpsessId;
    }

    private static void printHeadersByUrlConnection(URLConnection urlConnection) {
        Map<String, List<String>> headerFields = urlConnection.getHeaderFields();
        for (Map.Entry<String, List<String>> next : headerFields.entrySet()) {
            String key = next.getKey();
            if(key != null && key.equals("Set-Cookie")) {
                List<String> value = next.getValue();
                System.out.println("key:" + key);
                System.out.println("value:" + value);
            }
        }
    }

    private static void clientRequest(String vin, String captchaWord, String phpsessid) throws IOException {
        String client = "http://www.gibdd.ru/proxy/check/auto/2.0/client.php";
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
        String cookie = String.format("PHPSESSID=%s; BITRIX_SM_IP_REGKOD=77; BITRIX_SM_REGKOD=00; BITRIX_SM_METOD=GEO;", phpsessid);
        urlConnection.setRequestProperty("Cookie", cookie);

        // Send post request
        urlConnection.setDoOutput(true);
        DataOutputStream wr = new DataOutputStream(urlConnection.getOutputStream());
        String urlParameters = String.format("vin=%s&captchaWord=%s&captchaCode=", vin, captchaWord);
        wr.writeBytes(urlParameters);
        wr.flush();
        wr.close();


        int responseCode = urlConnection.getResponseCode();
        System.out.println("\nSending 'POST' request to URL : " + url);
        System.out.println("Post parameters : " + urlParameters);
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

    private static void saveImage(String phpsessId, String destinationFile) throws IOException {
        String imageUrl = "http://www.gibdd.ru/proxy/check/getCaptcha.php?PHPSESSID=" + phpsessId;
        URL url = new URL(imageUrl);
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

        String cookie = String.format("captchaSessionId=%s; _ym_uid=1462452903560071241; PHPSESSID=%s; _ym_isad=1; _ga=GA1.2.74263411.1462452903; BITRIX_SM_REGKOD=00; BITRIX_SM_IP_REGKOD=77; siteType=pda", phpsessId, phpsessId);
        urlConnection.setRequestProperty("Cookie", cookie);

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
    }
}