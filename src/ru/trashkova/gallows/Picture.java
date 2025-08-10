package ru.trashkova.gallows;

public class Picture {

    private static final String[] PICTURES = {
            """
    ------   
    |       
    |       
    |       
    |
    |       
    ---------- 
    """,
            """
     ------   
     |    |   
     |    O   
     |       
     |
     |       
     ---------- 
     """,
            """
    ------   
    |    |   
    |    O   
    |    |   
    |
    |       
    ----------
    """,
            """
    ------   
    |    |   
    |    O   
    |   /|   
    |
    |       
    ----------           
     """,
            """
    ------   
    |    |   
    |    O   
    |   /|\\   
    |
    |       
    -----------           
     """,
            """
    ------   
    |    |   
    |    O   
    |   /|\\   
    |  _/
    |   
    ----------           
     """,
            """
    ------   
    |    |   
    |    O   
    |   /|\\   
    |  _/ \\_ 
    |  
    ----------           
     """
    };

    public static void printPicture(int numPicture) {
        System.out.println(PICTURES[numPicture]);
    }

}