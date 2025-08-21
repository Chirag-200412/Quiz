import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.sql.*;
import java.util.*;

public class QuizGUI extends JFrame implements ActionListener {
    private static final String URL = "jdbc:mysql://localhost:3306/quizdb";
    private static final String USER = "root";
    private static final String PASSWORD = "Chirag@2004";

    private JLabel questionLabel;
    private JRadioButton[] options = new JRadioButton[4];
    private ButtonGroup bg;
    private JButton nextBtn, submitBtn;

    private ArrayList<Question> questions;
    private int currentQIndex = 0;
    private int score = 0;

    public QuizGUI() {
        setTitle("Quiz Application");
        setSize(600, 400);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new BorderLayout(10, 10));
        setLocationRelativeTo(null);

        questionLabel = new JLabel("Question will appear here");
        questionLabel.setFont(new Font("Arial", Font.BOLD, 16));
        add(questionLabel, BorderLayout.NORTH);

        JPanel optionsPanel = new JPanel(new GridLayout(4, 1, 10, 10));
        bg = new ButtonGroup();
        for (int i = 0; i < 4; i++) {
            options[i] = new JRadioButton();
            bg.add(options[i]);
            optionsPanel.add(options[i]);
        }
        add(optionsPanel, BorderLayout.CENTER);

        JPanel btnPanel = new JPanel();
        nextBtn = new JButton("Next");
        submitBtn = new JButton("Submit");
        nextBtn.addActionListener(this);
        submitBtn.addActionListener(this);
        btnPanel.add(nextBtn);
        btnPanel.add(submitBtn);
        add(btnPanel, BorderLayout.SOUTH);

        questions = fetchQuestionsFromDB();
        if (!questions.isEmpty()) {
            loadQuestion(currentQIndex);
        }
    }

    private ArrayList<Question> fetchQuestionsFromDB() {
        ArrayList<Question> qList = new ArrayList<>();
        try (Connection conn = DriverManager.getConnection(URL, USER, PASSWORD)) {
            String query = "SELECT * FROM questions ORDER BY RAND() LIMIT 5";
            Statement stmt = conn.createStatement();
            ResultSet rs = stmt.executeQuery(query);
            while (rs.next()) {
                qList.add(new Question(
                    rs.getString("question"),
                    new String[]{
                        rs.getString("option1"),
                        rs.getString("option2"),
                        rs.getString("option3"),
                        rs.getString("option4")
                    },
                    rs.getInt("correct_option")
                ));
            }
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Database Error: " + e.getMessage());
        }
        return qList;
    }

    private void loadQuestion(int index) {
        bg.clearSelection();
        Question q = questions.get(index);
        questionLabel.setText("Q" + (index + 1) + ": " + q.text);
        for (int i = 0; i < 4; i++) {
            options[i].setText(q.options[i]);
        }
    }

    private int getSelectedOption() {
        for (int i = 0; i < 4; i++) {
            if (options[i].isSelected()) {
                return i + 1;
            }
        }
        return -1;
    }

    public void actionPerformed(ActionEvent e) {
        if (e.getSource() == nextBtn) {
            int ans = getSelectedOption();
            if (ans == -1) {
                JOptionPane.showMessageDialog(this, "Please select an answer!");
                return;
            }
            if (ans == questions.get(currentQIndex).correctOption) {
                score++;
            }
            currentQIndex++;
            if (currentQIndex < questions.size()) {
                loadQuestion(currentQIndex);
            } else {
                showResult();
            }
        } else if (e.getSource() == submitBtn) {
            showResult();
        }
    }

    private void showResult() {
        JOptionPane.showMessageDialog(this, "Your Score: " + score + "/" + questions.size());
        System.exit(0);
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            new QuizGUI().setVisible(true);
        });
    }
}

class Question {
    String text;
    String[] options;
    int correctOption;

    Question(String text, String[] options, int correctOption) {
        this.text = text;
        this.options = options;
        this.correctOption = correctOption;
    }
}
