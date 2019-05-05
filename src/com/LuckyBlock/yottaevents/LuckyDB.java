//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by Fernflower decompiler)
//

package com.LuckyBlock.yottaevents;

import com.LuckyBlock.Engine.LuckyBlock;
import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import com.LuckyBlock.LB.LBType;
import org.bukkit.Bukkit;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.inventory.ItemStack;

public class LuckyDB {
    private static File dbFile;
    private static Map<UUID, LuckyDB.BlockData> db;
    private static boolean toSave;

    public LuckyDB() {
    }

    public static void checkSave() {
        if (toSave) {
            toSave = false;
            YamlConfiguration dbConfig = new YamlConfiguration();
            dbConfig.set("lb-item-list", db.entrySet().stream().map((e) -> {
                return ((UUID)e.getKey()).toString() + ":" + ((LuckyDB.BlockData)e.getValue()).amount + ":" + ((LuckyDB.BlockData)e.getValue()).timeMillis + ":" + ((LuckyDB.BlockData)e.getValue()).getPlace();
            }).collect(Collectors.toList()));

            try {
                dbConfig.save(dbFile);
            } catch (IOException var2) {
                var2.printStackTrace();
            }
        }

    }

    public static void loadConfig() {
        if (!dbFile.exists()) {
            try {
                dbFile.createNewFile();
            } catch (IOException var10) {
                var10.printStackTrace();
            }
        }

        long current = System.currentTimeMillis();
        YamlConfiguration dbConfig = new YamlConfiguration();

        try {
            dbConfig.load(dbFile);
            List<String> datas = dbConfig.getStringList("lb-item-list");
            db = new HashMap(datas.size() + 16);
            Iterator var4 = datas.iterator();

            while(var4.hasNext()) {
                String data = (String)var4.next();

                try {
                    String[] meta = data.split(":");
                    long timeMillis = Long.parseLong(meta[2]);
                    if (current - timeMillis > TimeUnit.DAYS.toMillis(60L)) {
                        toSave = true;
                    } else {
                        UUID uuid = UUID.fromString(meta[0]);
                        int amount = Integer.parseInt(meta[1]);
                        if (amount > 64) {
                            amount = 64;
                            toSave = true;
                        }
                        db.put(uuid, new LuckyDB.BlockData(uuid, amount, timeMillis, Integer.parseInt(meta[3])));
                    }
                } catch (Exception var9) {
                    LuckyBlock.instance.getLogger().severe("Invalid '" + data + "' in anti-dupe-database.yml");
                    var9.printStackTrace();
                }
            }
        } catch (InvalidConfigurationException | IOException var11) {
            var11.printStackTrace();
        }

    }

    public static void setToSave(boolean toSave) {
        LuckyDB.toSave = toSave;
    }

    public static Map<UUID, LuckyDB.BlockData> getDB() {
        return db;
    }

    public static BlockData getData(ItemStack stack) {
        if (!ItemStackUtils.isNullOrAir(stack) && LBType.isLB(stack)) {
            String uuidString = ItemStackUtils.getMetadata(stack);
            if (uuidString == null) {
                return null;
            }
            UUID uuid = UUID.fromString(uuidString);
            return getDB().get(uuid);
        } else {
            return null;
        }
    }

    public static void makeLucky(ItemStack item, int amount) {
        amount = Math.min(64, amount);
        UUID uuid = UUID.randomUUID();
        ItemStackUtils.setMetadata(item, uuid.toString());

        BlockData data = new BlockData(uuid, amount, System.currentTimeMillis(), 0);
        LuckyDB.getDB().put(uuid, data);
        LuckyDB.setToSave(true);
    }

    static {
        dbFile = new File(LuckyBlock.instance.getDataFolder(), "anti-dupe-database.yml");
        toSave = false;
        Bukkit.getScheduler().runTaskTimer(LuckyBlock.instance, LuckyDB::checkSave, 12000L, 12000L);
        loadConfig();
    }

    public static class BlockData {
        private UUID uuid;
        private int amount;
        private int place;
        private long timeMillis;

        public BlockData(UUID uuid, int amount, long timeMillis, int place) {
            this.uuid = uuid;
            this.amount = amount;
            this.timeMillis = timeMillis;
            this.place = place;
        }

        public UUID getUuid() {
            return uuid;
        }

        public int getAmount() {
            return this.amount;
        }

        public void setAmount(int amount) {
            this.amount = amount;
        }

        public int getPlace() {
            return this.place;
        }

        public void setPlace(int place) {
            this.place = place;
        }

        public long getTimeMillis() {
            return this.timeMillis;
        }

        public void setTimeMillis(long timeMillis) {
            this.timeMillis = timeMillis;
        }
    }
}
