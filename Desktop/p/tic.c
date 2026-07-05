#include <stdio.h>

char board[3][3];
char currentPlayer = 'X';

// Initialize board
void initializeBoard() {
    char ch = '1';
    for (int i = 0; i < 3; i++) {
        for (int j = 0; j < 3; j++) {
            board[i][j] = ch++;
        }
    }
}

// Display board
void displayBoard() {
    printf("\n");
    for (int i = 0; i < 3; i++) {
        printf(" %c | %c | %c ", board[i][0], board[i][1], board[i][2]);
        if (i < 2)
            printf("\n-----------\n");
    }
    printf("\n");
}

// Make move
int makeMove(int choice) {
    int row = (choice - 1) / 3;
    int col = (choice - 1) % 3;

    if (choice < 1 || choice > 9)
        return 0;

    if (board[row][col] == 'X' || board[row][col] == 'O')
        return 0;

    board[row][col] = currentPlayer;
    return 1;
}

// Check winner
int checkWinner() {
    // Rows and Columns
    for (int i = 0; i < 3; i++) {
        if (board[i][0] == board[i][1] &&
            board[i][1] == board[i][2])
            return 1;

        if (board[0][i] == board[1][i] &&
            board[1][i] == board[2][i])
            return 1;
    }

    // Diagonals
    if (board[0][0] == board[1][1] &&
        board[1][1] == board[2][2])
        return 1;

    if (board[0][2] == board[1][1] &&
        board[1][1] == board[2][0])
        return 1;

    return 0;
}

// Check draw
int checkDraw() {
    for (int i = 0; i < 3; i++)
        for (int j = 0; j < 3; j++)
            if (board[i][j] != 'X' && board[i][j] != 'O')
                return 0;
    return 1;
}

int main() {
    int choice;

    initializeBoard();

    while (1) {
        displayBoard();
        printf("\nPlayer %c, enter your choice (1-9): ", currentPlayer);
        scanf("%d", &choice);

        if (!makeMove(choice)) {
            printf("Invalid move! Try again.\n");
            continue;
        }

        if (checkWinner()) {
            displayBoard();
            printf("\nPlayer %c Wins!\n", currentPlayer);
            break;
        }

        if (checkDraw()) {
            displayBoard();
            printf("\nIt's a Draw!\n");
            break;
        }

        currentPlayer = (currentPlayer == 'X') ? 'O' : 'X';
    }

    return 0;
}