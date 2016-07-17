import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;
import java.util.List;
import java.util.Map;

/**
 * Получить капчу и jsessionid
 */
public class CaptchaService {
    public static void main(String[] args) throws IOException {
        CaptchaResult captchaResult = captchaRequest("new.jpg");
    }

    /**
     * Получить капчу и jsessionid
     * @param captchaFileName for example "new.jpg"
     * @return CaptchaResult image captcha with jsessionid
     * @throws IOException
     */
    private static CaptchaResult captchaRequest(String captchaFileName) throws IOException {
        URL url = new URL("http://check.gibdd.ru/proxy/captcha.jpg");
        HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();
        urlConnection.setRequestMethod("GET");
        urlConnection.setRequestProperty("User-Agent", CommonRequest.USER_AGENT);
        urlConnection.setRequestProperty("X-Compress", "0");
        urlConnection.setRequestProperty("Referer", "http://www.gibdd.ru/check/auto/");
        urlConnection.setRequestProperty("Host", "www.gibdd.ru");
        urlConnection.setRequestProperty("Accept", "image/webp,image/*,*/*;q=0.8");
        urlConnection.setRequestProperty("Accept-Encoding", "gzip, deflate, sdch");
        urlConnection.setRequestProperty("Accept-Language", "ru,en-US;q=0.8,en;q=0.6");
        urlConnection.setRequestProperty("Connection", "keep-alive");
        urlConnection.setRequestProperty("Cookie", "_ga=GA1.2.1593191729.1468750977; _ym_uid=1468750977525215266; _ym_isad=1");

        System.out.println("do request captcha");
        saveImage(captchaFileName, urlConnection.getInputStream());

        RequestUtil.printHeadersByUrlConnection(urlConnection);

        String sessionId = getSessionIdByURLConnection(urlConnection);
        CaptchaResult captchaResult = new CaptchaResult();
        captchaResult.setSessionId(sessionId);
        // captchaResult.setImage(Bitmap image);


        return captchaResult;
    }

    private static String getSessionIdByURLConnection(URLConnection urlConnection) {
        System.out.println("start get session id by URLConnection");
        Map<String, List<String>> headerFields = urlConnection.getHeaderFields();

        System.out.println("find session id in headerFields");
        for (Map.Entry<String, List<String>> next : headerFields.entrySet()) {
            String key = next.getKey();
            if (RequestUtil.isCookie(key)) {
                return findSessionId(next.getValue());
            }
        }

        throw new NotFoundResult(String.format("can't find %s id", CommonRequest.JSESSIONID));
    }

    private static void saveImage(String captchaFileName, InputStream is) throws IOException {
        OutputStream os = new FileOutputStream(captchaFileName);
        byte[] b = new byte[2048];
        int length;

        while ((length = is.read(b)) != -1) {
            os.write(b, 0, length);
        }

        is.close();
        os.close();
    }

    private static String findSessionId(List<String> value) {
        for (String s : value) {
            if (s.startsWith(CommonRequest.JSESSIONID)) {
                int start = s.indexOf("=") + 1;
                int endIndex = s.indexOf(";");
                String result = s.substring(start, endIndex);
                System.out.println(CommonRequest.JSESSIONID + ": " + result);
                return result;
            }
        }
        throw new NotFoundResult(String.format("can't find %s id", CommonRequest.JSESSIONID));
    }
}
