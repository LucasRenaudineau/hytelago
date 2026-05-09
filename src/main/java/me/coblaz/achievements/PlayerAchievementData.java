package me.coblaz.achievements;

public class PlayerAchievementData {

    private int count;
    private AchievementStatus status;

    public PlayerAchievementData() {
        this.count  = 0;
        this.status = AchievementStatus.NOT_DONE;
    }

    public int              getCount()              { return count; }
    public AchievementStatus getStatus()            { return status; }

    public void setCount(int count)                 { this.count = Math.max(0, count); }
    public void setStatus(AchievementStatus status) { this.status = status; }
    public void incrementCount(int amount)          { this.count += amount; }
}