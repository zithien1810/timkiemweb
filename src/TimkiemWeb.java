import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.net.URI;
import java.util.Arrays;
import java.util.List;
import java.util.Random;

import org.jsoup.Jsoup;
import org.jsoup.Connection;
import org.json.*;

public class TimkiemWeb extends JFrame {

    private JTextField txtKeyword;
    private JButton btnSearchNews, btnSearchProduct;
    private JTable tableNews, tableProduct;
    private DefaultTableModel modelNews, modelProduct;

    // Danh sách proxy dạng host:port
    private List<String> proxyList = Arrays.asList(
            "34.123.12.1:8080",
            "51.89.24.2:3128"
    );

    public TimkiemWeb() {
        setTitle("Tìm Tin Tức & Tìm kiếm sản phẩm");
        setSize(1200, 600);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLayout(new BorderLayout());

        // Top panel
        JPanel topPanel = new JPanel();
        JLabel lbl = new JLabel("Nhập từ khóa:");
        txtKeyword = new JTextField(30);
        btnSearchNews = new JButton("Tìm Tin Tức");
        btnSearchProduct = new JButton("Tìm kiếm sản phẩm");
        topPanel.add(lbl);
        topPanel.add(txtKeyword);
        topPanel.add(btnSearchNews);
        topPanel.add(btnSearchProduct);
        add(topPanel, BorderLayout.NORTH);

        // TabbedPane
        JTabbedPane tabbedPane = new JTabbedPane();

        // Tab Tin tức
        modelNews = new DefaultTableModel();
        modelNews.addColumn("Tiêu đề");
        modelNews.addColumn("Nguồn");
        modelNews.addColumn("Link");
        tableNews = new JTable(modelNews);
        tabbedPane.addTab("Tin tức", new JScrollPane(tableNews));

        // Tab Tìm kiếm sản phẩm
        modelProduct = new DefaultTableModel();
        modelProduct.addColumn("Tên sản phẩm");
        modelProduct.addColumn("Giá gốc");
        modelProduct.addColumn("Giá hiện tại");
        modelProduct.addColumn("Nguồn");
        modelProduct.addColumn("Link");
        tableProduct = new JTable(modelProduct);
        tabbedPane.addTab("Tìm kiếm sản phẩm", new JScrollPane(tableProduct));

        add(tabbedPane, BorderLayout.CENTER);

        // Click mở link
        tableNews.addMouseListener(new MouseAdapter() {
            public void mouseClicked(MouseEvent e) {
                int row = tableNews.getSelectedRow();
                if (row != -1) openLink(modelNews.getValueAt(row, 2).toString());
            }
        });

        tableProduct.addMouseListener(new MouseAdapter() {
            public void mouseClicked(MouseEvent e) {
                int row = tableProduct.getSelectedRow();
                if (row != -1) openLink(modelProduct.getValueAt(row, 4).toString());
            }
        });

        btnSearchNews.addActionListener(e -> searchNews());
        btnSearchProduct.addActionListener(e -> searchTiki());
    }

    private void openLink(String url){
        if(url!=null && !url.isEmpty()){
            try{ Desktop.getDesktop().browse(new URI(url)); }
            catch(Exception ex){ JOptionPane.showMessageDialog(this,"Không mở được link!"); }
        }
    }

    // ================= Tin tức =================
    private void searchNews(){
        modelNews.setRowCount(0);
        String keyword = txtKeyword.getText().trim();
        if(keyword.isEmpty()){ JOptionPane.showMessageDialog(this,"Nhập từ khóa!"); return; }

        new Thread(() -> {
            try{
                fetchNews("https://dantri.com.vn/tim-kiem.htm?keywords="+keyword,"Dân Trí","article.item-news","h3 a");
                fetchNews("https://timkiem.vnexpress.net/?q="+keyword,"VNExpress",".item-news","h3 a");
                fetchNews("https://vietnamnet.vn/tim-kiem?keyword="+keyword,"Vietnamnet",".vnn-container","a");
                JOptionPane.showMessageDialog(this,"Đã cập nhật tin tức!");
            }catch(Exception ex){ ex.printStackTrace(); JOptionPane.showMessageDialog(this,"Lỗi tìm tin tức!"); }
        }).start();
    }

    private void fetchNews(String url,String source,String itemClass,String titleSelector){
        try{
            org.jsoup.nodes.Document doc = Jsoup.connect(url)
                    .userAgent("Mozilla/5.0 (Windows NT 10.0; Win64; x64)")
                    .timeout(15000)
                    .get();
            doc.select(itemClass).forEach(item -> {
                try {
                    String text = item.selectFirst(titleSelector).text();
                    String link = item.selectFirst(titleSelector).absUrl("href");
                    SwingUtilities.invokeLater(() -> modelNews.addRow(new Object[]{text, source, link}));
                }catch(Exception ignored){}
            });
        }catch(Exception ex){ ex.printStackTrace(); }
    }

    // ================= Sản phẩm Tiki (proxy + retry + giá gốc + giá giảm) =================
    private void searchTiki(){
        modelProduct.setRowCount(0);
        String keyword = txtKeyword.getText().trim();
        if(keyword.isEmpty()){ JOptionPane.showMessageDialog(this,"Nhập từ khóa!"); return; }

        new Thread(() -> {
            try{
                fetchTikiWithRetry(keyword,3);
                JOptionPane.showMessageDialog(this,"Đã cập nhật giá sản phẩm Tiki!");
            }catch(Exception ex){ ex.printStackTrace(); JOptionPane.showMessageDialog(this,"Lỗi khi tìm sản phẩm Tiki!"); }
        }).start();
    }

    private void fetchTikiWithRetry(String keyword, int maxRetry){
        int attempt=0;
        boolean success=false;
        while(attempt<maxRetry && !success){
            try{
                fetchTiki(keyword, getRandomProxy());
                success=true;
            }catch(Exception ex){
                attempt++;
                System.out.println("Attempt "+attempt+" lỗi: "+ex.getMessage());
                if(attempt>=maxRetry) JOptionPane.showMessageDialog(this,"Không lấy được dữ liệu Tiki sau "+maxRetry+" lần.");
            }
        }
    }

    private String getRandomProxy(){
        if(proxyList.isEmpty()) return null;
        Random r = new Random();
        return proxyList.get(r.nextInt(proxyList.size()));
    }

    private void fetchTiki(String keyword, String proxy) throws Exception{
        String api="https://tiki.vn/api/v2/products?limit=20&q="+keyword;
        Connection conn = Jsoup.connect(api)
                .ignoreContentType(true)
                .header("User-Agent","Mozilla/5.0 (Windows NT 10.0; Win64; x64)")
                .header("x-requested-with","XMLHttpRequest")
                .timeout(15000);
        if(proxy!=null){
            String[] p = proxy.split(":");
            conn.proxy(p[0], Integer.parseInt(p[1]));
        }
        Connection.Response res = conn.execute();
        String jsonStr = res.body();
        System.out.println("JSON Tiki: "+jsonStr); // Debug JSON
        JSONObject obj = new JSONObject(jsonStr);
        JSONArray data = obj.getJSONArray("data");

        for(int i=0;i<data.length();i++){
            JSONObject product = data.getJSONObject(i);
            String name = product.getString("name");

            int price = product.getInt("price");
            int originalPrice = product.optInt("list_price", price); // Giá gốc, nếu không có dùng giá hiện tại
            SwingUtilities.invokeLater(() -> modelProduct.addRow(
                    new Object[]{name, originalPrice+"₫", price+"₫", "Tiki", product.getString("url")}
            ));
        }
    }

    public static void main(String[] args){
        SwingUtilities.invokeLater(() -> new TimkiemWeb().setVisible(true));
    }
}
