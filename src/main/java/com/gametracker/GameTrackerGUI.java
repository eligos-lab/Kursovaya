package com.gametracker;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.datatransfer.StringSelection;
import java.net.URI;
import java.util.List;
import java.util.HashMap;
import java.util.Map;

public class GameTrackerGUI extends JFrame {
    private CheapSharkService cheapSharkService;
    private JTextField searchField;
    private JButton searchButton;
    private JTable resultsTable;
    private JProgressBar progressBar;
    private DefaultTableModel tableModel;

    // Хранилище для ссылок
    private Map<Integer, String> rowToDealLinkMap;

    public GameTrackerGUI() {
        cheapSharkService = new CheapSharkService();
        rowToDealLinkMap = new HashMap<>();
        initializeUI();
    }

    private void initializeUI() {
        setTitle("🎮 Game Price Tracker");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(1200, 700);
        setLocationRelativeTo(null);

        // Основная панель
        JPanel mainPanel = new JPanel(new BorderLayout(10, 10));
        mainPanel.setBorder(new EmptyBorder(10, 10, 10, 10));

        // Панель поиска
        JPanel searchPanel = new JPanel(new BorderLayout(10, 10));
        searchField = new JTextField();
        searchButton = new JButton("🔍 Поиск");

        searchPanel.add(new JLabel("Название игры:"), BorderLayout.WEST);
        searchPanel.add(searchField, BorderLayout.CENTER);
        searchPanel.add(searchButton, BorderLayout.EAST);

        // Таблица результатов
        String[] columnNames = {"Название", "Самая низкая цена", "Ссылка"};
        tableModel = new DefaultTableModel(columnNames, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return column == 2; // Только колонка "Ссылка" будет кликабельной
            }

            @Override
            public Class<?> getColumnClass(int columnIndex) {
                if (columnIndex == 2) {
                    return JButton.class;
                }
                return String.class;
            }
        };

        resultsTable = new JTable(tableModel);
        resultsTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        resultsTable.setRowHeight(30);

        // Настройка ширины колонок
        resultsTable.getColumnModel().getColumn(0).setPreferredWidth(400); // Название
        resultsTable.getColumnModel().getColumn(1).setPreferredWidth(150); // Цена
        resultsTable.getColumnModel().getColumn(2).setPreferredWidth(150); // Ссылка

        // Кастомный рендерер для колонки с ссылками
        resultsTable.getColumnModel().getColumn(2).setCellRenderer(new LinkRenderer());
        resultsTable.getColumnModel().getColumn(2).setCellEditor(new LinkEditor(new JCheckBox()));

        JScrollPane tableScrollPane = new JScrollPane(resultsTable);

        // Панель информации
        JPanel infoPanel = new JPanel(new BorderLayout());
        JLabel infoLabel = new JLabel("Готов к поиску");
        progressBar = new JProgressBar();
        progressBar.setVisible(false);
        progressBar.setIndeterminate(true);

        infoPanel.add(infoLabel, BorderLayout.WEST);
        infoPanel.add(progressBar, BorderLayout.CENTER);

        // Добавление компонентов
        mainPanel.add(searchPanel, BorderLayout.NORTH);
        mainPanel.add(tableScrollPane, BorderLayout.CENTER);
        mainPanel.add(infoPanel, BorderLayout.SOUTH);

        add(mainPanel);

        // Обработчики событий
        setupEventHandlers();
    }

    private void setupEventHandlers() {
        // Поиск по кнопке
        searchButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                performSearch();
            }
        });

        // Поиск по Enter
        searchField.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                performSearch();
            }
        });

        // Обработка кликов по таблице
        resultsTable.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                int row = resultsTable.rowAtPoint(e.getPoint());
                int col = resultsTable.columnAtPoint(e.getPoint());

                if (row >= 0 && col == 2) { // Клик по колонке "Ссылка"
                    openDealLink(row);
                }
            }
        });
    }

    private void performSearch() {
        String gameName = searchField.getText().trim();
        if (gameName.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Пожалуйста, введите название игры",
                    "Ошибка", JOptionPane.WARNING_MESSAGE);
            return;
        }

        // Очищаем предыдущие результаты
        tableModel.setRowCount(0);
        rowToDealLinkMap.clear();

        // Показываем прогресс
        progressBar.setVisible(true);
        searchButton.setEnabled(false);

        // Запускаем поиск в отдельном потоке
        new Thread(() -> {
            try {
                List<GameDeal> games = cheapSharkService.searchGames(gameName);

                // Обновляем UI в EDT
                SwingUtilities.invokeLater(() -> {
                    updateResultsTable(games, gameName);
                    progressBar.setVisible(false);
                    searchButton.setEnabled(true);
                });
            } catch (Exception ex) {
                SwingUtilities.invokeLater(() -> {
                    progressBar.setVisible(false);
                    searchButton.setEnabled(true);
                    JOptionPane.showMessageDialog(this,
                            "Ошибка при поиске: " + ex.getMessage(),
                            "Ошибка", JOptionPane.ERROR_MESSAGE);
                });
            }
        }).start();
    }

    private void updateResultsTable(List<GameDeal> games, String searchQuery) {
        tableModel.setRowCount(0); // Очищаем таблицу
        rowToDealLinkMap.clear();

        int validGames = 0;

        for (GameDeal game : games) {
            if (game.getExternal() == null) continue;

            // ПРОСТАЯ ЛОГИКА: используем только то, что точно работает
            if (game.getCheapest() != null && game.getCheapestDealID() != null) {
                String price = String.format("$%s", game.getCheapest());
                String dealLink = cheapSharkService.getDealLink(game.getCheapestDealID());

                int row = tableModel.getRowCount();
                tableModel.addRow(new Object[]{
                        game.getExternal(),
                        price,
                        "🔗 Открыть"
                });

                // Сохраняем ссылку для этой строки
                rowToDealLinkMap.put(row, dealLink);
                validGames++;

                // Отладочная информация
                System.out.println("🎯 " + game.getExternal() + " | $" + game.getCheapest() + " | DealID: " + game.getCheapestDealID());
            }
        }

        // Итоговая информация
        System.out.println("=== РЕЗУЛЬТАТЫ ===");
        System.out.println("Найдено игр: " + validGames);
        System.out.println("==================");

        if (validGames == 0) {
            JOptionPane.showMessageDialog(this,
                    "Игры с названием '" + searchQuery + "' не найдены",
                    "Результат поиска", JOptionPane.INFORMATION_MESSAGE);
        } else {
            setTitle("🎮 Game Price Tracker - Найдено: " + validGames + " игр");
        }
    }

    private void openDealLink(int row) {
        String dealLink = rowToDealLinkMap.get(row);

        if (dealLink != null) {
            try {
                Desktop desktop = Desktop.isDesktopSupported() ? Desktop.getDesktop() : null;
                if (desktop != null && desktop.isSupported(Desktop.Action.BROWSE)) {
                    // МГНОВЕННОЕ ОТКРЫТИЕ БЕЗ ДИАЛОГА
                    desktop.browse(new URI(dealLink));

                    // Логируем для отладки
                    String gameName = (String) tableModel.getValueAt(row, 0);
                    String price = (String) tableModel.getValueAt(row, 1);
                    System.out.println("🌐 Открыта ссылка: " + gameName + " | " + price + " | " + dealLink);

                } else {
                    // Если браузер недоступен, копируем в буфер обмена
                    StringSelection stringSelection = new StringSelection(dealLink);
                    java.awt.datatransfer.Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
                    clipboard.setContents(stringSelection, null);

                    // Показываем уведомление только в случае ошибки
                    JOptionPane.showMessageDialog(this,
                            "Ссылка скопирована в буфер обмена:\n" + dealLink,
                            "Ссылка скопирована",
                            JOptionPane.INFORMATION_MESSAGE);
                }
            } catch (Exception e) {
                // Показываем ошибку только если не удалось открыть
                JOptionPane.showMessageDialog(this,
                        "Не удалось открыть браузер. Ссылка скопирована в буфер обмена:\n" + dealLink,
                        "Ошибка",
                        JOptionPane.WARNING_MESSAGE);
            }
        }
    }

    // Кастомный рендерер для ссылок
    private class LinkRenderer extends JButton implements javax.swing.table.TableCellRenderer {
        public LinkRenderer() {
            setOpaque(true);
            setForeground(Color.BLUE);
            setCursor(new Cursor(Cursor.HAND_CURSOR));
            setBorderPainted(false);
            setContentAreaFilled(false);
            setFocusPainted(false);
        }

        @Override
        public Component getTableCellRendererComponent(JTable table, Object value,
                                                       boolean isSelected, boolean hasFocus, int row, int column) {
            setText((value == null) ? "" : value.toString());
            if (isSelected) {
                setBackground(table.getSelectionBackground());
                setForeground(table.getSelectionForeground());
            } else {
                setBackground(table.getBackground());
                setForeground(Color.BLUE);
            }
            return this;
        }
    }

    // Кастомный редактор для ссылок
    private class LinkEditor extends DefaultCellEditor {
        private JButton button;
        private String label;
        private boolean isPushed;
        private int currentRow;

        public LinkEditor(JCheckBox checkBox) {
            super(checkBox);
            button = new JButton();
            button.setOpaque(true);
            button.setForeground(Color.BLUE);
            button.setCursor(new Cursor(Cursor.HAND_CURSOR));
            button.setBorderPainted(false);
            button.setContentAreaFilled(false);
            button.setFocusPainted(false);
            button.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    fireEditingStopped();
                }
            });
        }

        @Override
        public Component getTableCellEditorComponent(JTable table, Object value,
                                                     boolean isSelected, int row, int column) {
            label = (value == null) ? "" : value.toString();
            button.setText(label);
            currentRow = row;
            isPushed = true;
            return button;
        }

        @Override
        public Object getCellEditorValue() {
            if (isPushed) {
                openDealLink(currentRow);
            }
            isPushed = false;
            return label;
        }

        @Override
        public boolean stopCellEditing() {
            isPushed = false;
            return super.stopCellEditing();
        }
    }

    public static void main(String[] args) {
        // Устанавливаем современный Look and Feel
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception e) {
            e.printStackTrace();
        }

        SwingUtilities.invokeLater(() -> {
            new GameTrackerGUI().setVisible(true);
        });
    }
}