import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

public class filterRules {
    private static final int BUFFER_SIZE = 8192; // 增加缓冲区大小
    private static final int THREAD_POOL_SIZE = 4; // 线程池大小

    public static void main(String[] args) {
        // 使用Collections.synchronizedSet包装ConcurrentHashSet以保证线程安全
        Set<String> res = ConcurrentHashMap.newKeySet();
        
        // 定义URL列表
        String[] urls = {
            "https://raw.githubusercontent.com/blackmatrix7/ios_rule_script/master/rule/AdGuard/Advertising/Advertising.txt",
            "https://raw.githubusercontent.com/217heidai/adblockfilters/main/rules/adblockdns.txt",
            "https://raw.githubusercontent.com/217heidai/adblockfilters/main/rules/adblockdnslite.txt",
            "https://adguardteam.github.io/HostlistsRegistry/assets/filter_29.txt"
        };

        ExecutorService executor = Executors.newFixedThreadPool(THREAD_POOL_SIZE);

        try {
            // 并行处理所有URL并收集Future对象
            List<Future<?>> futures = new ArrayList<>();
            for (String url : urls) {
                final String finalUrl = url;
                Future<?> future = executor.submit(() -> {
                    try {
                        mergeLines(finalUrl, res);
                    } catch (IOException e) {
                        System.err.println("读取URL时发生错误: " + finalUrl + ", 错误信息: " + e.getMessage());
                    }
                });
                futures.add(future);
            }

            // 关闭线程池并等待所有任务完成
            executor.shutdown();
            
            // 等待所有任务完成
            try {
                // 等待所有任务执行完毕
                for (Future<?> future : futures) {
                    try {
                        future.get(30, TimeUnit.SECONDS);
                    } catch (Exception e) {
                        System.err.println("任务执行出错: " + e.getMessage());
                    }
                }
            } catch (Exception e) {
                System.err.println("等待任务完成时发生错误: " + e.getMessage());
            }

            System.out.println(res.size());
            saveToFile(res, "filtered_results.txt");

        } catch (Exception e) {
            System.err.println("处理文件时发生错误: " + e.getMessage());
            e.printStackTrace();
        } finally {
            if (!executor.isShutdown()) {
                executor.shutdownNow();
            }
        }
    }

    /**
     * 读取文件或URL的所有行到列表中
     * 
     * @param source 文件路径或URL
     * @return 包含所有行的列表
     * @throws IOException 读取异常
     */
    private static Set<String> mergeLines(String source, Set<String> set) throws IOException {
        if (source.startsWith("http://") || source.startsWith("https://")) {
            return mergeUrlLines(source, set);
        } else {
            return mergeFileLines(source, set);
        }
    }

    /**
     * 读取文件的所有行到列表中
     * 
     * @param fileName 文件名
     * @return 包含文件所有行的列表
     * @throws IOException 文件读取异常
     */
    private static Set<String> mergeFileLines(String fileName, Set<String> lines) throws IOException {
        try (BufferedReader reader = new BufferedReader(new FileReader(fileName), BUFFER_SIZE)) {
            String line;
            while ((line = reader.readLine()) != null) {
                if (!line.isEmpty() && !line.startsWith("!")) {
                    lines.add(line);
                }
            }
        }
        return lines;
    }

    /**
     * 从URL读取所有行到列表中
     * 
     * @param urlString URL地址
     * @return 包含所有行的列表
     * @throws IOException 网络读取异常
     */
    private static Set<String> mergeUrlLines(String urlString, Set<String> lines) throws IOException {
        URL url = new URL(urlString);
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        connection.setRequestMethod("GET");
        // 设置连接和读取超时
        connection.setConnectTimeout(10000);
        connection.setReadTimeout(10000);
        // 设置请求头以获得更好的兼容性
        connection.setRequestProperty("User-Agent", "Mozilla/5.0");

        try (InputStream inputStream = connection.getInputStream();
                BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream), BUFFER_SIZE)) {
            String line;
            while ((line = reader.readLine()) != null) {
                if (!line.isEmpty() && !line.startsWith("!")) {
                    lines.add(line);
                }
            }
        }
        return lines;
    }

    /**
     * 将结果保存到文件中
     * 
     * @param lines 要保存的行集合
     * @param fileName 输出文件名
     * @throws IOException 写入异常
     */
    private static void saveToFile(Set<String> lines, String fileName) throws IOException {
        try (PrintWriter writer = new PrintWriter(fileName)) {
            // 使用普通顺序流而非并行流写入文件，提高写入效率
            lines.forEach(writer::println);
        }
        System.out.println("结果已保存到文件: " + fileName);
    }
}