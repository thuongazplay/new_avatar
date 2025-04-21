package avatar.Farm;

public class StarfruilLevelData {
    // Mảng chứa thông tin về giá nâng cấp, thời gian thu hoạch, và số trái tối đa theo từng cấp độ
    private static final int[][] LEVEL_DATA = {
        // {giá nâng cấp, thời gian thu hoạch (phút), số trái tối đa}
        {500, 2, 20}, {2000, 8, 25}, {4500, 18, 30}, {8000, 32, 35}, {12500, 50, 40},
        {18000, 72, 45}, {24500, 98, 50}, {32000, 128, 55}, {40500, 162, 60}, {55000, 300, 65},
        {66550, 363, 70}, {79200, 432, 75}, {92950, 507, 80}, {107800, 588, 85}, {123750, 675, 90},
        {140800, 768, 95}, {158950, 867, 100}, {178200, 972, 105}, {198550, 1083, 110}, {240000, 1600, 120},
        {264600, 1764, 130}, {290400, 1936, 140}, {317400, 2116, 150}, {345600, 2304, 160}, {375000, 2500, 170},
        {405600, 2704, 180}, {437400, 2916, 190}, {470400, 3136, 200}, {504600, 3364, 210}, {630000, 5400, 225},
        {672700, 5766, 240}, {716800, 6144, 255}, {762300, 6534, 270}, {809200, 6936, 285}, {857500, 7350, 300},
        {907200, 7776, 315}, {958300, 8214, 330}, {1010800, 8664, 345}, {1064700, 9126, 360}, {1600000, 16000, 380},
        {1681000, 16810, 400}, {1764000, 17640, 420}, {1849000, 18490, 440}, {1936000, 19360, 460}, {2025000, 20250, 480},
        {2116000, 21160, 500}, {2209000, 22090, 520}, {2304000, 23040, 540}, {2401000, 24010, 560}, {2500000, 25000, 600}
    };

    // Lấy giá nâng cấp theo cấp độ
    public static int getUpgradeCost(int level) {
        if (level >= 1 && level <= LEVEL_DATA.length) {
            return LEVEL_DATA[level - 1][0];
        }
        return 0; // Giá trị mặc định nếu cấp độ không hợp lệ
    }

    // Lấy thời gian thu hoạch (phút) theo cấp độ
    public static int getTimeToHarvest(int level) {
        if (level >= 1 && level <= LEVEL_DATA.length) {
            return LEVEL_DATA[level - 1][1];
        }
        return 0; // Giá trị mặc định nếu cấp độ không hợp lệ
    }

    // Lấy số trái tối đa theo cấp độ
    public static int getMaxFruits(int level) {
        if (level >= 1 && level <= LEVEL_DATA.length) {
            return LEVEL_DATA[level - 1][2];
        }
        return 0; // Giá trị mặc định nếu cấp độ không hợp lệ
    }
}
