package avatar.Farm;

import java.util.HashMap;
import java.util.Map;

public class LandUpgradeCost {
    public static final Map<Integer, Cost> landUpgradeLevel1 = new HashMap<>();
    public static final Map<Integer, Cost> landUpgradeLevel2 = new HashMap<>();
    static {
        landUpgradeLevel1.put(7, new Cost(10800, 10));
        landUpgradeLevel1.put(8, new Cost(14700, 14));
        landUpgradeLevel1.put(9, new Cost(19200, 19));
        landUpgradeLevel1.put(10, new Cost(24300, 24));
        landUpgradeLevel1.put(11, new Cost(30000, 30));
        landUpgradeLevel1.put(12, new Cost(36300, 36));
        landUpgradeLevel1.put(13, new Cost(43200, 43));
        landUpgradeLevel1.put(14, new Cost(50700, 50));
        landUpgradeLevel1.put(15, new Cost(58800, 58));
        landUpgradeLevel1.put(16, new Cost(67500, 67));
        landUpgradeLevel1.put(17, new Cost(76800, 76));
        landUpgradeLevel1.put(18, new Cost(86700, 86));
        landUpgradeLevel1.put(19, new Cost(97200, 97));
        landUpgradeLevel1.put(20, new Cost(108300, 108));
        landUpgradeLevel1.put(21, new Cost(120000, 120));
        landUpgradeLevel1.put(22, new Cost(132300, 132));
        landUpgradeLevel1.put(23, new Cost(145200, 145));
        landUpgradeLevel1.put(24, new Cost(158700, 158));
        landUpgradeLevel1.put(25, new Cost(172800, 172));
        landUpgradeLevel1.put(26, new Cost(187500, 187));
        landUpgradeLevel1.put(27, new Cost(202800, 202));
        landUpgradeLevel1.put(28, new Cost(218700, 218));
        landUpgradeLevel1.put(29, new Cost(235200, 235));
        landUpgradeLevel1.put(30, new Cost(252300, 252));
        landUpgradeLevel1.put(31, new Cost(270000, 270));
        landUpgradeLevel1.put(32, new Cost(288300, 288));
        landUpgradeLevel1.put(33, new Cost(307200, 307));
        landUpgradeLevel1.put(34, new Cost(326700, 326));
        landUpgradeLevel1.put(35, new Cost(346800, 346));
        landUpgradeLevel1.put(36, new Cost(367500, 367));
        landUpgradeLevel1.put(37, new Cost(388800, 388));
        landUpgradeLevel1.put(38, new Cost(410700, 410));
        landUpgradeLevel1.put(39, new Cost(433200, 433));
        landUpgradeLevel1.put(40, new Cost(456300, 456));
        landUpgradeLevel1.put(41, new Cost(480000, 480));
        landUpgradeLevel1.put(42, new Cost(504300, 504));
        landUpgradeLevel1.put(43, new Cost(529200, 529));
        landUpgradeLevel1.put(44, new Cost(554700, 554));
        landUpgradeLevel1.put(45, new Cost(580800, 580));
        landUpgradeLevel1.put(46, new Cost(607500, 607));
        landUpgradeLevel1.put(47, new Cost(634800, 634));
        landUpgradeLevel1.put(48, new Cost(662700, 662));

        landUpgradeLevel2.put(0, new Cost(580800, 580));
        landUpgradeLevel2.put(1, new Cost(607500, 607));
        landUpgradeLevel2.put(2, new Cost(634800, 634));
        landUpgradeLevel2.put(3, new Cost(662700, 662));
    }

    public static Cost getCostForLevel1(int level) {
        return landUpgradeLevel1.get(level);
    }
    public static Cost getCostForLevel2(int level) {
        return landUpgradeLevel2.get(level);
    }


    public static class Cost {
        private final int xu;
        private final int luong;

        public Cost(int xu, int luong) {
            this.xu = xu;
            this.luong = luong;
        }

        public int getXu() {
            return xu;
        }

        public int getLuong() {
            return luong;
        }

        @Override
        public String toString() {
            return String.format("%d xu hoặc %d lượng", xu, luong);
        }
    }
}

