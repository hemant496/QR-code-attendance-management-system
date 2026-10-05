package model;

import java.sql.Date;

public class Holiday {
    private int id;
    private Date holidayDate;
    private String description;

    public Holiday() {}

    public Holiday(int id, Date holidayDate, String description) {
        this.id = id;
        this.holidayDate = holidayDate;
        this.description = description;
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public Date getHolidayDate() { return holidayDate; }
    public void setHolidayDate(Date holidayDate) { this.holidayDate = holidayDate; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
}
