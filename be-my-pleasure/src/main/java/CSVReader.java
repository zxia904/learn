import cn.hutool.core.collection.CollectionUtil;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;
import org.apache.commons.io.input.BOMInputStream;
import org.apache.commons.lang3.CharEncoding;
import org.apache.commons.lang3.StringUtils;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Dictionary;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;

public class CSVReader {

    public static List<String> ConsultHeaders = Arrays.asList("isbn","书名","出版社");

    public static void main(String[] args) {

        CSVReader csvReader = new CSVReader();
        Dictionary<String, Integer> consultHeaders = new Hashtable<>(ConsultHeaders.size());
        List<CSVRecord> csvRecords = csvReader.validateHeader("C:/Users/FD/Desktop/工作簿1.csv", consultHeaders);

        List<CsvVO> csvVOS = csvReader.resolveRecords(csvRecords, consultHeaders);

        System.out.println(csvVOS);
    }

    private List<CsvVO> resolveRecords(List<CSVRecord> records, Dictionary<String,Integer> consultHeaders) {
        if (CollectionUtil.isEmpty(records)){
            throw new RuntimeException("数据为空");
        }

        List<CsvVO> result = new ArrayList<>(records.size());
        CsvVO tmp;
        for (int i = 0; i < records.size(); i++) {
            CSVRecord record = records.get(i);

            String isbn = record.get(consultHeaders.get(ConsultHeaders.get(0)));
            String title = record.get(consultHeaders.get(ConsultHeaders.get(1)));
            String publisher = record.get(consultHeaders.get(ConsultHeaders.get(2)));

            if (StringUtils.isAnyEmpty(isbn, title)) {
                throw new RuntimeException(String.format("第%d行[isbn]或[书籍名称]为空", i+1));
            }
            tmp = new CsvVO(isbn, title, publisher);

            result.add(tmp);
        }

        return result;

    }

    /**
     * 验证格式是否正确
     *
     * @return
     */
    private List<CSVRecord> validateHeader(String filePath, Dictionary<String, Integer> consultHeaderMap) {

        CSVParser csvParser;
        try {

            Reader reader;
            BOMInputStream bomInputStream = new BOMInputStream(new FileInputStream(filePath));
            if (bomInputStream.hasBOM()) {
                reader = new BufferedReader(new InputStreamReader(bomInputStream, CharEncoding.UTF_8));
            } else {
                reader = new BufferedReader(new InputStreamReader(new FileInputStream(filePath), "GBK"));
            }

            csvParser = new CSVParser(reader, CSVFormat.DEFAULT.withFirstRecordAsHeader().withAllowMissingColumnNames().withTrim());
            Map<String, Integer> headerMap = csvParser.getHeaderMap();
            if (CollectionUtil.isEmpty(headerMap)) {
                throw new RuntimeException("没有找到表头，请检查后再导入");
            }

            for (String header : ConsultHeaders) {
                if (!headerMap.containsKey(header)) {
                    throw new RuntimeException(MessageFormat.format("没有找到{0}，请检查后再导入", header));
                } else {
                    consultHeaderMap.put(header, headerMap.get(header));
                }
            }

            return csvParser.getRecords();
        } catch (IOException e) {
            e.printStackTrace();
        }

        return null;
    }


    public static class CsvVO {

        private String isbn;

        private String ebookName;

        private String  publisher;

        public CsvVO(String isbn, String ebookName, String publisher) {
            this.isbn = isbn;
            this.ebookName = ebookName;
            this.publisher = publisher;
        }

        public String getIsbn() {
            return isbn;
        }

        public void setIsbn(String isbn) {
            this.isbn = isbn;
        }

        public String getEbookName() {
            return ebookName;
        }

        public void setEbookName(String ebookName) {
            this.ebookName = ebookName;
        }

        public String getPublisher() {
            return publisher;
        }

        public void setPublisher(String publisher) {
            this.publisher = publisher;
        }

        @Override
        public String toString() {
            return "CsvVO{" +
                    "isbn='" + isbn + '\'' +
                    ", ebookName='" + ebookName + '\'' +
                    ", publisher='" + publisher + '\'' +
                    '}';
        }
    }
}
