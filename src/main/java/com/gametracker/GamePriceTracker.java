package com.gametracker;

import com.gametracker.GameDeal;
import com.gametracker.Deal;
import com.gametracker.CheapSharkService;
import java.util.*;

public class GamePriceTracker {
    private CheapSharkService cheapSharkService;
    private Scanner scanner;

    public GamePriceTracker() {
        this.cheapSharkService = new CheapSharkService();
        this.scanner = new Scanner(System.in);
    }

    public void start() {
        System.out.println("=== Game Price Tracker ===");
        System.out.println("Монитор цен на игры с использованием CheapShark API");

        try {
            while (true) {
                System.out.println("\nВведите название игры (или 'quit' для выхода):");

                if (!scanner.hasNextLine()) {
                    System.out.println("Ввод недоступен. Завершение работы.");
                    break;
                }

                String gameName = scanner.nextLine();

                if (gameName.equalsIgnoreCase("quit")) {
                    break;
                }

                if (gameName.trim().isEmpty()) {
                    System.out.println("Пожалуйста, введите название игры.");
                    continue;
                }

                searchAndDisplayDeals(gameName);
            }
        } catch (Exception e) {
            System.out.println("Произошла ошибка: " + e.getMessage());
        } finally {
            System.out.println("Спасибо за использование Game Price Tracker!");
            if (scanner != null) {
                scanner.close();
            }
        }
    }

    private void searchAndDisplayDeals(String gameName) {
        System.out.println("\nПоиск сделок для: " + gameName);

        List<GameDeal> games = cheapSharkService.searchGames(gameName);

        if (games == null || games.isEmpty()) {
            System.out.println("Игры с названием '" + gameName + "' не найдены.");
            return;
        }

        System.out.println("Найдено игр: " + games.size());

        for (GameDeal game : games) {
            if (game.getExternal() == null) continue;

            System.out.println("\n" + "=".repeat(50));
            System.out.println(game.getExternal().toUpperCase());

            List<Deal> deals = cheapSharkService.getGameDeals(game.getGameID());

            if (deals.isEmpty() || !hasValidPrices(deals)) {
                displayBasicGameInfo(game);
            } else {
                displayAllDeals(deals, game);
            }
        }
    }

    private boolean hasValidPrices(List<Deal> deals) {
        for (Deal deal : deals) {
            try {
                if (deal.getSalePrice() != null && !deal.getSalePrice().isEmpty()) {
                    Double.parseDouble(deal.getSalePrice());
                    return true;
                }
            } catch (NumberFormatException e) {
                // Продолжаем проверку
            }
        }
        return false;
    }

    private void displayBasicGameInfo(GameDeal game) {
        System.out.printf("Самая низкая цена: $%s%n", game.getCheapest());
        if (game.getCheapestDealID() != null && !game.getCheapestDealID().isEmpty()) {
            System.out.println("Ссылка: " + cheapSharkService.getDealLink(game.getCheapestDealID()));
        }
    }

    private void displayAllDeals(List<Deal> deals, GameDeal game) {
        Deal bestDeal = null;
        double bestPrice = Double.MAX_VALUE;

        List<Deal> validDeals = new ArrayList<>();
        for (Deal deal : deals) {
            try {
                if (deal.getSalePrice() != null && !deal.getSalePrice().isEmpty()) {
                    Double.parseDouble(deal.getSalePrice());
                    validDeals.add(deal);
                }
            } catch (NumberFormatException e) {
                // Пропускаем сделки с некорректными ценами
            }
        }

        if (validDeals.isEmpty()) {
            displayBasicGameInfo(game);
            return;
        }

        validDeals.sort(Comparator.comparing(deal -> Double.parseDouble(deal.getSalePrice())));

        int counter = 1;
        for (Deal deal : validDeals) {
            String storeName = cheapSharkService.getStoreName(deal.getStoreID());
            double price = Double.parseDouble(deal.getSalePrice());
            String dealLink = cheapSharkService.getDealLink(deal.getDealID());

            String discountInfo = "";
            if (deal.getSavings() != null && !deal.getSavings().isEmpty()) {
                try {
                    double savings = Double.parseDouble(deal.getSavings());
                    if (savings > 0) {
                        discountInfo = String.format(" (-%.0f%%)", savings);
                    }
                } catch (NumberFormatException e) {
                    // Игнорируем ошибки парсинга скидки
                }
            }

            System.out.printf("%d. $%.2f%s (%s)%n",
                    counter++, price, discountInfo, storeName);
            System.out.printf("   Ссылка: %s%n", dealLink);

            if (price < bestPrice) {
                bestPrice = price;
                bestDeal = deal;
            }
        }

        if (bestDeal != null) {
            String bestStoreName = cheapSharkService.getStoreName(bestDeal.getStoreID());
            String bestDealLink = cheapSharkService.getDealLink(bestDeal.getDealID());
            System.out.println("─".repeat(30));
            System.out.println("ИТОГ:");
            System.out.printf("🎯 Самая выгодная цена: $%.2f (%s)%n", bestPrice, bestStoreName);
            System.out.printf("🔗 Ссылка: %s%n", bestDealLink);
        }
    }

    public static void main(String[] args) {
        GamePriceTracker tracker = new GamePriceTracker();
        tracker.start();
    }
}