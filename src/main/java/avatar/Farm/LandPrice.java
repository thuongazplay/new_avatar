package avatar.Farm;

public class LandPrice {
    // Danh sách giá trị xu cho các ô đất từ 7 -> 48
    private static final int[] xuValues = {
        10800, 14700, 19200, 24300, 30000, 36300, 43200, 50700, 58800, 67500,
        76800, 86700, 97200, 108300, 120000, 132300, 145200, 158700, 172800, 187500,
        202800, 218700, 235200, 252300, 270000, 288300, 307200, 326700, 346800, 367500,
        388800, 410700, 433200, 456300, 480000, 504300, 529200, 554700, 580800, 607500,
        634800, 662700
    };

    // Danh sách giá trị lượng cho các ô đất từ 7 -> 48
    private static final int[] luongValues = {
        10, 14, 19, 24, 30, 36, 43, 50, 58, 67,
        76, 86, 97, 108, 120, 132, 145, 158, 172, 187,
        202, 218, 235, 252, 270, 288, 307, 326, 346, 367,
        388, 410, 433, 456, 480, 504, 529, 554, 580, 607,
        634, 662
    };

    // Lấy giá trị xu cho ô đất theo số thứ tự (index bắt đầu từ 7)
    public static int getXuForLand(int landIndex) {
        if (landIndex >= 7 && landIndex <= 48) {
            return xuValues[landIndex - 7];
        }
        return 0; // Giá trị mặc định nếu ô đất không hợp lệ
    }

    // Lấy giá trị lượng cho ô đất theo số thứ tự (index bắt đầu từ 7)
    public static int getLuongForLand(int landIndex) {
        if (landIndex >= 7 && landIndex <= 48) {
            return luongValues[landIndex - 7];
        }
        return 0; // Giá trị mặc định nếu ô đất không hợp lệ
    }
}
