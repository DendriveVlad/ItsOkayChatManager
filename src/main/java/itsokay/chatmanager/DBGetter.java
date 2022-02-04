package itsokay.chatmanager;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class DBGetter {
    public DataBase sql = new DataBase();

    public String getLanguage(String nick) throws SQLException, ClassNotFoundException {
        PreparedStatement ps = sql.doGet("SELECT language FROM minecraft WHERE nick=?");
        ps.setString(1, nick);
        ResultSet result = ps.executeQuery();
        String ans = "";
        if (result.next()) {
            ans = result.getString("language");
        }
        return ans;
    }

    public void setReply(String message_from, String message_to) throws SQLException, ClassNotFoundException {
        PreparedStatement ps = sql.doGet("UPDATE minecraft SET last_pm=? WHERE nick=?");
        ps.setString(1, message_from);
        ps.setString(2, message_to);
        ps.executeUpdate();
        PreparedStatement ps2 = sql.doGet("UPDATE minecraft SET last_pm=? WHERE nick=?");
        ps2.setString(1, message_to);
        ps2.setString(2, message_from);
        ps2.executeUpdate();
    }

    public String getReply(String nick) throws SQLException, ClassNotFoundException {
        PreparedStatement ps = sql.doGet("SELECT last_pm FROM minecraft WHERE nick=?");
        ps.setString(1, nick);
        ResultSet result = ps.executeQuery();
        String ans = "";
        if (result.next()) {
            ans = result.getString("last_pm");
        }
        return ans;
    }

    public void addMessage(int id, String player, String message) throws SQLException, ClassNotFoundException {
        PreparedStatement ps = sql.doGet("INSERT INTO messages (id, author, message) VALUES (?, ?, ?)");
        ps.setInt(1, id);
        ps.setString(2, player);
        ps.setString(3, message);
        ps.executeUpdate();
    }

    public int getMessageID() throws SQLException, ClassNotFoundException {
        PreparedStatement ps = sql.doGet("SELECT id FROM messages");
        ResultSet result = ps.executeQuery();
        int id = new Random().nextInt(999);
        while (result.next()) {
            while (id == result.getInt("id")) {
                id = new Random().nextInt(999);
            }
        }
        return id;
    }

    public String[] getMessage(int id) throws SQLException, ClassNotFoundException {
        PreparedStatement ps = sql.doGet("SELECT message, author FROM messages WHERE id=?");
        ps.setInt(1, id);
        ResultSet result = ps.executeQuery();
        String[] ans = new String[]{"", ""};
        if (result.next()) {
            ans[0] = result.getString("message");
            ans[1] = result.getString("author");
        }
        return ans;
    }

    public void clearMessages() throws SQLException, ClassNotFoundException {
        PreparedStatement ps = sql.doGet("DELETE FROM messages");
        ps.executeUpdate();
    }

    public void deleteMessages() throws SQLException, ClassNotFoundException {
        PreparedStatement ps = sql.doGet("SELECT COUNT(*) FROM messages");
        ResultSet result = ps.executeQuery();
        if (result.next()) {
            int len = result.getInt(1);
            if (len <= 100) {
                return;
            }
            ps = sql.doGet("SELECT id FROM messages");
            result = ps.executeQuery();
            PreparedStatement del;
            while (result.next() && len != 90) {
                del = sql.doGet("DELETE FROM messages WHERE id=?");
                del.setInt(1, result.getInt("id"));
                del.executeUpdate();
                len--;
            }
        }
    }

    public void updateNotify(String player, int turn) throws SQLException, ClassNotFoundException {
        PreparedStatement ps = sql.doGet("UPDATE minecraft SET notification=? WHERE nick=?");
        ps.setInt(1, turn);
        ps.setString(2, player);
        ps.executeUpdate();
    }

    public boolean getNotifyAccept(String player) throws SQLException, ClassNotFoundException {
        PreparedStatement notify = sql.doGet("SELECT notification FROM minecraft WHERE nick=?");
        notify.setString(1, player);
        ResultSet result = notify.executeQuery();
        if (result.next()) {
            return result.getInt("notification") == 1;
        }
        return false;
    }

    public String getClanTag(String player) throws SQLException, ClassNotFoundException {
        PreparedStatement clan = sql.doGet("SELECT clan FROM minecraft WHERE nick=?");
        clan.setString(1, player);
        ResultSet result = clan.executeQuery();
        if (result.next()) {
            int clan_id = result.getInt("clan");
            PreparedStatement tag = sql.doGet("SELECT tag, color FROM clans WHERE id=?");
            tag.setInt(1, clan_id);
            ResultSet result_tag = tag.executeQuery();
            if (result_tag.next()) {
                return "§" + result_tag.getString("color") + result_tag.getString("tag");
            }
        }
        return "";
    }

    public int getClanID(String player) throws SQLException, ClassNotFoundException {
        PreparedStatement clan = sql.doGet("SELECT clan FROM minecraft WHERE nick=?");
        clan.setString(1, player);
        ResultSet result = clan.executeQuery();
        if (result.next()) {
            return result.getInt("clan");
        }
        return 0;
    }

    public String[] getClanInfo(int id) throws SQLException, ClassNotFoundException {
        PreparedStatement clan = sql.doGet("SELECT name, owner_name, tag, color FROM clans WHERE id=?");
        clan.setInt(1, id);
        ResultSet result = clan.executeQuery();
        if (result.next()) {
            PreparedStatement members = sql.doGet("SELECT moder FROM minecraft WHERE clan=?");
            members.setInt(1, id);
            ResultSet res = members.executeQuery();
            int len = 0;
            while (res.next()) {
                len++;
            }
            return new String[]{result.getString("name"), result.getString("owner_name"), result.getString("tag"), result.getString("color"), String.valueOf(len)};
        }
        return new String[0];
    }

    public boolean checkClanName(String name) throws SQLException, ClassNotFoundException {
        PreparedStatement clans_names = sql.doGet("SELECT id FROM clans WHERE name=?");
        clans_names.setString(1, name);
        ResultSet result = clans_names.executeQuery();
        return result.next();
    }

    public boolean checkClanTag(String teg) throws SQLException, ClassNotFoundException {
        PreparedStatement clans_names = sql.doGet("SELECT id FROM clans WHERE tag=?");
        clans_names.setString(1, teg);
        ResultSet result = clans_names.executeQuery();
        return result.next();
    }

    public boolean checkHaveClan(String name) throws SQLException, ClassNotFoundException {
        PreparedStatement clans_names = sql.doGet("SELECT clan FROM minecraft WHERE nick=?");
        clans_names.setString(1, name);
        ResultSet result = clans_names.executeQuery();
        if (result.next()) {
            return result.getInt("clan") != 0;
        }
        return false;
    }

    public void createClan(String name, String owner_name, ConfigManager config) throws SQLException, ClassNotFoundException {
        PreparedStatement ps = sql.doGet("SELECT id FROM clans");
        ResultSet result = ps.executeQuery();
        int id = new Random().nextInt(99999);
        while (result.next()) {
            while (id == result.getInt("id")) {
                id = new Random().nextInt(99999);
            }
        }
        PreparedStatement clan = sql.doGet("INSERT INTO clans (id, name, owner_name) VALUES (?, ?, ?)");
        clan.setInt(1, id);
        clan.setString(2, name);
        clan.setString(3, owner_name);
        clan.executeUpdate();

        PreparedStatement owner = sql.doGet("UPDATE minecraft SET clan=? WHERE nick=?");
        owner.setInt(1, id);
        owner.setString(2, owner_name);
        owner.executeUpdate();

        config.getConfig("players").set("players." + owner_name + ".clan_id", id);
    }

    public boolean isClanOwner(String name) throws SQLException, ClassNotFoundException {
        PreparedStatement clan_owner = sql.doGet("SELECT id FROM clans WHERE owner_name=?");
        clan_owner.setString(1, name);
        ResultSet result = clan_owner.executeQuery();
        return result.next();
    }

    public void deleteClan(String name) throws SQLException, ClassNotFoundException {
        PreparedStatement del = sql.doGet("DELETE FROM clans WHERE owner_name=?");
        del.setString(1, name);
        del.executeUpdate();

        PreparedStatement members = sql.doGet("UPDATE minecraft SET clan=? WHERE nick=?");
        members.setInt(1, 0);
        members.setString(2, name);
        members.executeUpdate();
    }

    public void inviteToClan(String name, String owner_name) throws SQLException, ClassNotFoundException {
        PreparedStatement members = sql.doGet("UPDATE minecraft SET clan_invite=? WHERE nick=?");
        members.setString(1, owner_name);
        members.setString(2, name);
        members.executeUpdate();
    }

    public boolean checkHaveInvite(String name, String invitor) throws SQLException, ClassNotFoundException {
        PreparedStatement clan_owner = sql.doGet("SELECT clan_invite FROM minecraft WHERE nick=?");
        clan_owner.setString(1, name);
        ResultSet result = clan_owner.executeQuery();
        if (result.next()) {
            return result.getString("clan_invite").equalsIgnoreCase(invitor);
        }
        return false;
    }

    public void acceptInvite(String name, String invitor, ConfigManager config) throws SQLException, ClassNotFoundException {
        PreparedStatement invite = sql.doGet("UPDATE minecraft SET clan=? WHERE nick=?");
        int clan_id = config.getConfig("players").getInt("players." + invitor + ".clan_id");
        invite.setInt(1, clan_id);
        invite.setString(3, name);
        invite.executeUpdate();

        config.getConfig("players").set("players." + name + ".clan_invite", "");
        config.getConfig("players").set("players." + name + ".clan_id", clan_id);
        config.getConfig("players").set("players." + name + ".clan", config.getConfig("players").getInt("players." + invitor + ".clan"));
    }

    public void delInvite(String player) throws SQLException, ClassNotFoundException {
        PreparedStatement ps;
        if (!player.equals("")) {
            ps = sql.doGet("UPDATE minecraft SET clan_invite=? WHERE nick=?");
            ps.setString(1, "");
            ps.setString(2, player);
        } else {
            ps = sql.doGet("UPDATE minecraft SET clan_invite=?");
            ps.setString(1, "");
        }
        ps.executeUpdate();
    }

    public void leaveClan(String name, ConfigManager config) throws SQLException, ClassNotFoundException {
        PreparedStatement del = sql.doGet("UPDATE minecraft SET clan=? WHERE nick=?");
        del.setInt(1, 0);
        del.setString(2, name);
        del.executeUpdate();

        config.getConfig("players").set("players." + name + ".clan_id", 0);
        config.getConfig("players").set("players." + name + ".clan", "§e");
    }

    public List<String> getClanMembers(String member) throws SQLException, ClassNotFoundException {
        int id = getClanID(member);
        PreparedStatement members = sql.doGet("SELECT nick FROM minecraft WHERE clan=?");
        members.setInt(1, id);
        ResultSet res = members.executeQuery();
        List<String> players = new ArrayList<>();
        while (res.next()) {
            players.add(res.getString("nick"));
        }
        return players;
    }

    public void renameClan(String new_name, String owner_name) throws SQLException, ClassNotFoundException {
        PreparedStatement name = sql.doGet("UPDATE clans SET name=? WHERE id=?");
        name.setString(1, new_name);
        name.setInt(2, getClanID(owner_name));
        name.executeUpdate();
    }

    public void promoteClanMember(String member) throws SQLException, ClassNotFoundException {
        PreparedStatement new_member = sql.doGet("UPDATE clans SET owner_name=? WHERE id=?");
        new_member.setString(1, member);
        new_member.setInt(2, getClanID(member));
        new_member.executeUpdate();
    }

    public void setClanTag(String owner, String tag) throws SQLException, ClassNotFoundException {
        PreparedStatement new_member = sql.doGet("UPDATE clans SET tag=? WHERE id=?");
        new_member.setString(1, tag);
        new_member.setInt(2, getClanID(owner));
        new_member.executeUpdate();
    }

    public void setClanColor(String owner, String color) throws SQLException, ClassNotFoundException {
        PreparedStatement new_member = sql.doGet("UPDATE clans SET color=? WHERE id=?");
        new_member.setString(1, color);
        new_member.setInt(2, getClanID(owner));
        new_member.executeUpdate();
    }

    // ignore_chat 0 - открытый чат, 1 - закрытое лс, 2 - закрытый глобальный чат, 3 - закрытое всё
    public String[] getPlayerIgnores(String player) throws SQLException, ClassNotFoundException {
        PreparedStatement members = sql.doGet("SELECT ignore_chat, ignore_players FROM minecraft WHERE nick=?");
        members.setString(1, player);
        ResultSet res = members.executeQuery();
        if (res.next()) {
            return new String[]{String.valueOf(res.getInt("ignore_chat")), res.getString("ignore_players")};
        }
        return new String[2];
    }

    public void setPrivate(String player, int level) throws SQLException, ClassNotFoundException {
        PreparedStatement new_member = sql.doGet("UPDATE minecraft SET ignore_chat=? WHERE nick=?");
        new_member.setInt(1, level);
        new_member.setString(2, player);
        new_member.executeUpdate();
    }

    public void addIgnore(String player, String ignore_player) throws SQLException, ClassNotFoundException {
        PreparedStatement new_member = sql.doGet("UPDATE minecraft SET ignore_players=? WHERE nick=?");
        new_member.setString(1, getPlayerIgnores(player)[1] + ignore_player.toLowerCase() + " ");
        new_member.setString(2, player);
        new_member.executeUpdate();
    }

    public void removeIgnore(String player, String ignore_player) throws SQLException, ClassNotFoundException {
        PreparedStatement new_member = sql.doGet("UPDATE minecraft SET ignore_players=? WHERE nick=?");
        new_member.setString(1, getPlayerIgnores(player)[1].replace(ignore_player.toLowerCase() + " ", ""));
        new_member.setString(2, player);
        new_member.executeUpdate();
    }

    public boolean isModer(String nick) throws SQLException, ClassNotFoundException {
        PreparedStatement ps = sql.doGet("SELECT nick FROM moders WHERE nick=?");
        ps.setString(1, nick);
        ResultSet result = ps.executeQuery();
        return result.next();
    }

    public void setTargetPlayer(String mod, String targetPlayer) throws SQLException, ClassNotFoundException {
        PreparedStatement new_member = sql.doGet("UPDATE moders SET target=? WHERE nick=?");
        new_member.setString(1, targetPlayer);
        new_member.setString(2, mod);
        new_member.executeUpdate();
    }

    public String getTargetPlayer(String mod) throws SQLException, ClassNotFoundException {
        PreparedStatement ps = sql.doGet("SELECT target FROM moders WHERE nick=?");
        ps.setString(1, mod);
        ResultSet result = ps.executeQuery();
        if (result.next()) {
            return result.getString("target");
        }
        return "";
    }

    public boolean ban(String mod, String player, String reason) throws SQLException, ClassNotFoundException {
        PreparedStatement lj = sql.doGet("SELECT last_join FROM moders WHERE nick=?");
        lj.setString(1, mod);
        ResultSet result = lj.executeQuery();
        long member = 0;
        if (result.next()) {
            member = result.getLong("last_join");
        }
        PreparedStatement bans = sql.doGet("SELECT bans_last_minute, last_ban FROM minecraft WHERE member=?");
        bans.setLong(1, member);
        int bans_last_minute = 0;
        long last_ban = 0L;
        if (result.next()) {
            bans_last_minute = result.getInt("bans_last_minute");
            last_ban = result.getLong("last_ban");
        }
        long now_time = System.currentTimeMillis() / 1000;
        if (bans_last_minute >= 3) {
            PreparedStatement ban_moder = sql.doGet("UPDATE minecraft SET banned=?, ban_reason='Подозрительные действия' WHERE member=?");
            ban_moder.setLong(1, now_time);
            ban_moder.setLong(2, member);
            ban_moder.executeUpdate();
            return false;
        }
        PreparedStatement ban_moder;
        if (last_ban < 61) {
            ban_moder = sql.doGet("UPDATE minecraft SET bans_last_minute=?, last_ban=? WHERE member=?");
            bans_last_minute++;
            ban_moder.setInt(1, bans_last_minute);
            ban_moder.setLong(2, now_time);
            ban_moder.setLong(3, member);
        } else {
            ban_moder = sql.doGet("UPDATE minecraft SET bans_last_minute=0, last_ban=? WHERE member=?");
            ban_moder.setLong(1, now_time);
            ban_moder.setLong(2, member);
        }
        ban_moder.executeUpdate();
        PreparedStatement ban_player = sql.doGet("UPDATE minecraft SET banned=?, ban_reason=? WHERE nick=?");
        ban_player.setLong(1, now_time);
        ban_player.setString(2, reason);
        ban_player.setString(3, player);
        ban_player.executeUpdate();
        return true;
    }

    public void unban(String player) throws SQLException, ClassNotFoundException {
        PreparedStatement ban_player = sql.doGet("UPDATE minecraft SET banned=0, ban_reason='' WHERE nick=?");
        ban_player.setString(1, player);
        ban_player.executeUpdate();
    }

    public String getModerNick(String mod) throws SQLException, ClassNotFoundException {
        PreparedStatement lj = sql.doGet("SELECT last_join FROM moders WHERE nick=?");
        lj.setString(1, mod);
        ResultSet result = lj.executeQuery();
        long member = 0;
        if (result.next()) {
            member = result.getLong("last_join");
        }
        PreparedStatement member_ds = sql.doGet("SELECT nick FROM minecraft WHERE member=?");
        member_ds.setLong(1, member);
        ResultSet res = member_ds.executeQuery();
        if (res.next()) {
            return res.getString("nick");
        }
        return "";
    }

    public int getDonationState(String nick) throws SQLException, ClassNotFoundException {
        PreparedStatement ps = sql.doGet("SELECT donation FROM minecraft WHERE nick=?");
        ps.setString(1, nick);
        ResultSet result = ps.executeQuery();
        if (result.next()) {
            return result.getInt("donation");
        }
        return 0;
    }

    public String getNickColor(String nick) throws SQLException, ClassNotFoundException {
        PreparedStatement ps = sql.doGet("SELECT color FROM minecraft WHERE nick=?");
        ps.setString(1, nick);
        ResultSet result = ps.executeQuery();
        if (result.next()) {
            return result.getString("color");
        }
        return "a";
    }

    public void setNickColor(String nick, String color) throws SQLException, ClassNotFoundException {
        PreparedStatement ps = sql.doGet("UPDATE minecraft SET color=? WHERE nick=?");
        ps.setString(1, color);
        ps.setString(2, nick);
        ps.executeUpdate();
    }

    public void moderInfoClear(String nick) throws SQLException, ClassNotFoundException {
        PreparedStatement ps = sql.doGet("UPDATE moders SET last_join=0, online=0, join_accept=0, target='' WHERE nick=?");
        ps.setString(1, nick);
        ps.executeUpdate();
    }
}
