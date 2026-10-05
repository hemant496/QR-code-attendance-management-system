package dashboard;

import dao.AttendanceDAO;
import dao.StudentDAO;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.plot.PiePlot;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.data.category.DefaultCategoryDataset;
import org.jfree.data.general.DefaultPieDataset;
import utils.ThemeManager;

import javax.swing.*;
import javax.swing.border.LineBorder;
import java.awt.*;
import java.util.Map;

public class DashboardStatsPanel extends JPanel {
    private JLabel lblTotalStudents;
    private JLabel lblPresentToday;
    private JLabel lblAbsentToday;
    private JLabel lblAttendanceRate;
    
    private final StudentDAO studentDAO = new StudentDAO();
    private final AttendanceDAO attendanceDAO = new AttendanceDAO();
    
    private JPanel chart1Container;
    private JPanel chart2Container;

    public DashboardStatsPanel() {
        setLayout(new BorderLayout(15, 15));
        setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        
        initUI();
    }

    private void initUI() {
        // --- 1. Statistics Cards Panel (Top) ---
        JPanel cardsPanel = new JPanel(new GridLayout(1, 4, 15, 0));
        
        lblTotalStudents = new JLabel("0", JLabel.CENTER);
        lblPresentToday = new JLabel("0", JLabel.CENTER);
        lblAbsentToday = new JLabel("0", JLabel.CENTER);
        lblAttendanceRate = new JLabel("0.0%", JLabel.CENTER);

        cardsPanel.add(createCard("Total Students", lblTotalStudents, new Color(13, 110, 253))); // Blue
        cardsPanel.add(createCard("Present Today", lblPresentToday, new Color(25, 135, 84)));  // Green
        cardsPanel.add(createCard("Absent Today", lblAbsentToday, new Color(220, 53, 69)));   // Red
        cardsPanel.add(createCard("Attendance Rate", lblAttendanceRate, new Color(255, 193, 7))); // Yellow/Orange

        add(cardsPanel, BorderLayout.NORTH);

        // --- 2. Charts Panel (Center) ---
        JPanel chartsPanel = new JPanel(new GridLayout(1, 2, 20, 0));
        
        chart1Container = new JPanel(new BorderLayout());
        chart1Container.setBorder(BorderFactory.createTitledBorder("Today's Attendance Ratio"));
        
        chart2Container = new JPanel(new BorderLayout());
        chart2Container.setBorder(BorderFactory.createTitledBorder("Present Students by Department"));

        chartsPanel.add(chart1Container);
        chartsPanel.add(chart2Container);

        add(chartsPanel, BorderLayout.CENTER);
        
        refreshStats();
    }

    private JPanel createCard(String title, JLabel valueLabel, Color accentColor) {
        JPanel card = new JPanel(new BorderLayout(5, 5)) {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                // Draw a colored top border accent
                g.setColor(accentColor);
                g.fillRect(0, 0, getWidth(), 6);
            }
        };
        card.setBorder(new LineBorder(new Color(220, 220, 220), 1, true));
        card.setPreferredSize(new Dimension(180, 110));
        card.setBackground(ThemeManager.isDarkMode() ? new Color(43, 45, 48) : Color.WHITE);

        JLabel titleLabel = new JLabel(title, JLabel.CENTER);
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 14));
        titleLabel.setForeground(ThemeManager.isDarkMode() ? Color.LIGHT_GRAY : Color.GRAY);
        titleLabel.setBorder(BorderFactory.createEmptyBorder(15, 0, 0, 0));

        valueLabel.setFont(new Font("Segoe UI", Font.BOLD, 28));
        valueLabel.setForeground(ThemeManager.isDarkMode() ? Color.WHITE : Color.DARK_GRAY);
        valueLabel.setBorder(BorderFactory.createEmptyBorder(0, 0, 15, 0));

        card.add(titleLabel, BorderLayout.NORTH);
        card.add(valueLabel, BorderLayout.CENTER);

        return card;
    }

    public void refreshStats() {
        int totalStudents = studentDAO.getStudentCount();
        int presentToday = attendanceDAO.getTodayAttendanceCount("Present") + attendanceDAO.getTodayAttendanceCount("Late");
        int absentToday = totalStudents - presentToday;
        if (absentToday < 0) absentToday = 0; // Guard

        double rate = 0.0;
        if (totalStudents > 0) {
            rate = ((double) presentToday / totalStudents) * 100.0;
        }

        lblTotalStudents.setText(String.valueOf(totalStudents));
        lblPresentToday.setText(String.valueOf(presentToday));
        lblAbsentToday.setText(String.valueOf(absentToday));
        lblAttendanceRate.setText(String.format("%.1f%%", rate));

        // Recreate Charts
        createAttendanceRatioChart(presentToday, absentToday);
        createDepartmentStatsChart();
    }

    private void createAttendanceRatioChart(int present, int absent) {
        DefaultPieDataset dataset = new DefaultPieDataset();
        dataset.setValue("Present (" + present + ")", present);
        dataset.setValue("Absent (" + absent + ")", absent);

        JFreeChart chart = ChartFactory.createPieChart(
                null,
                dataset,
                true,
                true,
                false
        );

        // Customize Colors
        PiePlot plot = (PiePlot) chart.getPlot();
        plot.setSectionPaint("Present (" + present + ")", new Color(40, 167, 69));
        plot.setSectionPaint("Absent (" + absent + ")", new Color(220, 53, 69));
        plot.setBackgroundPaint(ThemeManager.isDarkMode() ? new Color(43, 45, 48) : Color.WHITE);
        plot.setOutlinePaint(null);
        
        chart.setBackgroundPaint(ThemeManager.isDarkMode() ? new Color(43, 45, 48) : Color.WHITE);
        
        ChartPanel chartPanel = new ChartPanel(chart);
        chartPanel.setBackground(ThemeManager.isDarkMode() ? new Color(43, 45, 48) : Color.WHITE);
        
        chart1Container.removeAll();
        chart1Container.add(chartPanel, BorderLayout.CENTER);
        chart1Container.revalidate();
        chart1Container.repaint();
    }

    private void createDepartmentStatsChart() {
        DefaultCategoryDataset dataset = new DefaultCategoryDataset();
        Map<String, Integer> deptStats = attendanceDAO.getDepartmentWiseStats();

        if (deptStats.isEmpty()) {
            dataset.addValue(0, "Present", "No Data");
        } else {
            for (Map.Entry<String, Integer> entry : deptStats.entrySet()) {
                dataset.addValue(entry.getValue(), "Present", entry.getKey());
            }
        }

        JFreeChart chart = ChartFactory.createBarChart(
                null,
                "Department",
                "Present Students",
                dataset,
                PlotOrientation.VERTICAL,
                false,
                true,
                false
        );

        chart.setBackgroundPaint(ThemeManager.isDarkMode() ? new Color(43, 45, 48) : Color.WHITE);
        chart.getCategoryPlot().setBackgroundPaint(ThemeManager.isDarkMode() ? new Color(30, 30, 30) : Color.WHITE);
        
        ChartPanel chartPanel = new ChartPanel(chart);
        chartPanel.setBackground(ThemeManager.isDarkMode() ? new Color(43, 45, 48) : Color.WHITE);
        
        chart2Container.removeAll();
        chart2Container.add(chartPanel, BorderLayout.CENTER);
        chart2Container.revalidate();
        chart2Container.repaint();
    }
}
