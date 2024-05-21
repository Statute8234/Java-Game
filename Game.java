import java.awt.*;
import java.awt.event.*;
import java.sql.Time;
import java.util.ArrayList;
import java.util.Random;
import java.util.TimerTask;
import java.util.Iterator;
import javax.swing.JPanel;
import javax.swing.Timer;

public class Game extends JPanel implements ActionListener, KeyListener{
    private Timer gameTimer;
    private Timer itemSpawnRate;
    private final int SpawnRate = 60000; // one minute
    private final double foodChance = 0.40;
    private final double weaponChance = 0.20;
    private final double levelupChance = 0.05;
    
    int WIDTH;
    int HEIGHT;
    int tileSize = 25;

    public enum ItemType {
        FOOD,
        WEAPON,
        LEVELUP
    }
    
    private class Tile {
        int x;
        int y;
        ItemType type;

        Tile(int x, int y, ItemType type) {
            this.x = x;
            this.y = y;
            this.type = type;

        }
    }

    // player
    Tile player;
    int playerLevel = 1;
    int playerOrigional_Damage = 1;
    int playerOrigional_Health = 100;
    int playerDamage = playerOrigional_Damage;
    int playerHealth = playerOrigional_Health;
    int player_velocityX = 0;
    int player_velocityY = 0;
    // monster
    Tile testMonster;
    int MonsterLevel = 1;
    int monsterOrigional_Damage = 1;
    int monsterOrigional_Health = 100;
    int monsterDamage = monsterOrigional_Damage;
    int monsterHealth = monsterOrigional_Health;
    ArrayList<Tile> monsterScope;
    Random randint = new Random();
    int[] targetPosition = new int[]{0, 0};
    int monster_velocityX = 1;
    int monster_velocityY = 1;
    // items
    ArrayList<Tile> itemsList;
    Tile foodTile;
    Tile weaponsTile;
    int weaponDamage = 1;
    Tile levelup_Tile;
    String levelup_name = "";

    Game(int WIDTH, int HEIGHT) {
        this.WIDTH = WIDTH;
        this.HEIGHT = HEIGHT;
        setPreferredSize(new Dimension(this.WIDTH, this.HEIGHT));
        setBackground(Color.BLACK);
        player = new Tile(0, 0, null);
        testMonster = new Tile(10, 10, null);
        monsterScope = createMonsterScope(testMonster);
        targetPosition = new int[] {randint.nextInt(WIDTH / tileSize), randint.nextInt(WIDTH / tileSize)};

        itemsList = new ArrayList<>();
        foodTile = new Tile(randint.nextInt(WIDTH / tileSize), randint.nextInt(WIDTH / tileSize), ItemType.FOOD);
        weaponsTile = new Tile(randint.nextInt(WIDTH / tileSize), randint.nextInt(WIDTH / tileSize), ItemType.WEAPON);
        levelup_Tile = new Tile(randint.nextInt(WIDTH / tileSize), randint.nextInt(WIDTH / tileSize), ItemType.LEVELUP);
        itemsList.add(foodTile);
        itemsList.add(weaponsTile);
        itemsList.add(levelup_Tile);

        gameTimer = new Timer(100, this);
        gameTimer.start();

        addKeyListener(this);
        setFocusable(true);
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        draw(g);
    }

    public void draw(Graphics g) {
        // gid
        g.setColor(Color.WHITE);
        for (int i = 0; i < WIDTH/tileSize; i++) {
            g.drawLine(i * tileSize, 0, i * tileSize, HEIGHT);
            g.drawLine(0, i * tileSize, WIDTH, i * tileSize);
        }
        // monser
        g.setColor(Color.white);
        g.fill3DRect(testMonster.x * tileSize, testMonster.y * tileSize, tileSize, tileSize, true);
        g.setColor(Color.darkGray);
        for (Tile tile : monsterScope) {
            g.fill3DRect(tile.x * tileSize, tile.y * tileSize, tileSize, tileSize, true);
        }

        // player
        g.setColor(Color.red);
        g.fill3DRect(player.x * tileSize, player.y * tileSize, tileSize, tileSize, true);
    
        // items
        for (Tile item : itemsList) {
            if (item == foodTile) {
                g.setColor(Color.RED);
            }
            else if (item == weaponsTile) {
                g.setColor(Color.gray);
            }
            else if (item == levelup_Tile) {
                g.setColor(Color.green);
            }
            g.fill3DRect(item.x * tileSize, item.y * tileSize, tileSize, tileSize, true);
        }
        // text
        drawText(playerHealth, player, g);
        drawText(monsterHealth, testMonster, g);
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        move();
        respon();
        monsterMMove();
        collectItems();
        repaint();
    }

    public void move() {
        collectItems();
        // -- monster
        boolean check = true;
        for (Tile tile : monsterScope) {
            if (collision(player, tile)) {
                check = false;
                damage(randint.nextInt(playerDamage + 1),randint.nextInt(monsterDamage + 1));
                break;
            }
        }

        if (check) {
            player.x += player_velocityX;
            player.y += player_velocityY;
            ensureWithinBounds(player);
        }
    }

    public void ensureWithinBounds(Tile tile) {
        if (tile.x < 0) tile.x = 0;
        if (tile.x >= WIDTH / tileSize) tile.x = WIDTH / tileSize - 1;
        if (tile.y < 0) tile.y = 0;
        if (tile.y >= HEIGHT / tileSize) tile.y = HEIGHT / tileSize - 1;
    }

    @Override
    public void keyPressed(KeyEvent e) {
        switch (e.getKeyCode()) {
            case KeyEvent.VK_UP:
                player_velocityX = 0;
                player_velocityY = -1;
                break;
            case KeyEvent.VK_DOWN:
                player_velocityX = 0;
                player_velocityY = 1;
                break;
            case KeyEvent.VK_LEFT:
                player_velocityX = -1;
                player_velocityY = 0;
                break;
            case KeyEvent.VK_RIGHT:
                player_velocityX = 1;
                player_velocityY = 0;
                break;
        }
    }

    @Override
    public void keyReleased(KeyEvent e) {
        // Stop movement when the key is released
        player_velocityX = 0;
        player_velocityY = 0;
    }

    @Override
    public void keyTyped(KeyEvent e) {
        // Not used but required by KeyListener
    }

    // --- monster --
    public boolean collision(Tile tile1, Tile tile2) {
        return tile1.x == tile2.x && tile1.y == tile2.y;
    }

    private ArrayList<Tile> createMonsterScope(Tile monster) {
        ArrayList<Tile> scope = new ArrayList<>();
        for (int i = -1; i <= 1; i++) {
            for (int j = -1; j <= 1; j++) {
                if (i != 0 || j != 0) {
                    scope.add(new Tile(monster.x + i, monster.y + j , null));
                }
            }
        }
        return scope;
    }

    public void monsterMMove() {
        boolean changePosition = false;
        boolean moveMonster = true;
        Random randint = new Random();

        for (Tile tile : monsterScope) {
            if (collision(tile, player)) {
                moveMonster = false;
                damage(randint.nextInt(playerDamage + 1),randint.nextInt(monsterDamage + 1));
                break;
            }
        }
        
        if (moveMonster) {
            if (testMonster.x != targetPosition[0] || testMonster.y != targetPosition[1]) {
                if (testMonster.x > targetPosition[0]) testMonster.x -= monster_velocityX;
                if (testMonster.x < targetPosition[0]) testMonster.x += monster_velocityX;
                if (testMonster.y > targetPosition[1]) testMonster.y -= monster_velocityY;
                if (testMonster.y < targetPosition[1]) testMonster.y += monster_velocityY;
                monsterScope = createMonsterScope(testMonster);
                ensureWithinBounds(testMonster);
            } else {
                changePosition = true;
            }
        }

        if (changePosition) {
            targetPosition = new int[] {randint.nextInt(WIDTH / tileSize), randint.nextInt(WIDTH / tileSize)};
        }
    }

    // --- text
    public void drawText(int num, Tile tile, Graphics g) {
        g.setColor(Color.white);
        g.setFont(new Font("Times", Font.BOLD, 14));
        if (num > 0) g.drawString("" + num, tile.x * tileSize, (tile.y * tileSize) + 1);
        if (num <= 0) g.drawString("Lost", tile.x * tileSize, (tile.y * tileSize) + 1);
    }

    public void damage(int num1, int num2) {
        if (playerHealth - num2 > 0 && monsterHealth - num1 > 0) {
            playerHealth -= num2;
            monsterHealth -= num1;
        } else {
            if (playerHealth - num2 <= 0) playerHealth = 0;
            if (monsterHealth - num1 <= 0) monsterHealth = 0;
        }
    }

    // items
    public void respon() {
        if (playerHealth <= 0) {
            player.x = randint.nextInt(WIDTH / tileSize);
            player.y = randint.nextInt(HEIGHT / tileSize);
            playerHealth = 100;
            playerOrigional_Health = 100;
            playerDamage = 0;
            playerOrigional_Damage = 0;
        }

        if (monsterHealth <= 0) {
            testMonster.x = randint.nextInt(WIDTH / tileSize);
            testMonster.y = randint.nextInt(HEIGHT / tileSize);
            monsterScope = createMonsterScope(testMonster);
            ensureWithinBounds(testMonster);
            monsterHealth = 100;
            monsterOrigional_Health = 100;
            monsterDamage = 0;
            monsterOrigional_Damage = 0;
        }
    }

    public void collectItems() {
        boolean can_spawnItems = false;
        Iterator<Tile> iterator = itemsList.iterator();
        while (iterator.hasNext()) {
            Tile item = iterator.next();
            if (collision(player, item)) {
                handleItemCollision(item, true);
                iterator.remove();
                can_spawnItems = true;
            }
            if (collision(testMonster, item)) {
                iterator.remove();
                can_spawnItems = true;
            }
        }

        if (can_spawnItems) spawnItems();
    }

    private void handleItemCollision(Tile item, boolean isPlayer) {
        switch (item.type) {
            case FOOD:
                if (isPlayer) {
                    playerHealth = Math.min(playerHealth + 10, playerOrigional_Health);
                } else {
                    monsterHealth = Math.min(monsterHealth + 10, monsterOrigional_Health);
                }
                break;
            case WEAPON:
                if (isPlayer) {
                    playerDamage = (playerOrigional_Damage + randint.nextInt(playerOrigional_Damage * 10,playerOrigional_Damage * 100));
                } else {
                    monsterDamage = (monsterOrigional_Damage + randint.nextInt(monsterOrigional_Damage * 10,monsterOrigional_Damage * 100));
                }
                break;
            case LEVELUP:
                if (isPlayer) {
                    playerLevel += 1;
                    playerOrigional_Damage += (playerOrigional_Damage * playerLevel);
                    playerOrigional_Health += (playerOrigional_Health * playerLevel);
                } else {
                    monsterOrigional_Damage *= 2;
                    monsterOrigional_Health *= 2;
                }
                break;
            default:
                break;
        }
    }

    private void spawnItems() {
        if (randint.nextDouble() < foodChance) {
            foodTile = new Tile(randint.nextInt(WIDTH / tileSize), randint.nextInt(WIDTH / tileSize), ItemType.FOOD);
            itemsList.add(foodTile);
        }
        if (randint.nextDouble() < weaponChance) {
            weaponsTile = new Tile(randint.nextInt(WIDTH / tileSize), randint.nextInt(WIDTH / tileSize), ItemType.WEAPON);
            itemsList.add(weaponsTile);
        }
        if (randint.nextDouble() < levelupChance) {
            levelup_Tile = new Tile(randint.nextInt(WIDTH / tileSize), randint.nextInt(WIDTH / tileSize), ItemType.LEVELUP);
            itemsList.add(levelup_Tile);
        }
    }

    public void removeItem(Tile item) {
        spawnItems();
        itemsList.remove(item);
    }
}