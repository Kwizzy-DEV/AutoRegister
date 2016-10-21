AUTO REGISTER
-------

**Introduction**

Forcé de constater que d'enregistrer ses événements dans la classe principale du plugin devient vraiment pénible à la longue, j'ai décidé de faire une classe qui fait des enregistrements de toutes les classes d'un coup grâce à une méthode. 
Cette classe est composé de plusieurs méthodes, une pour "customiser" votre enregistrement (imaginons que vous ayez des classes qui implements de Manageable et qu'elles aient une fonction .register, grâce à mon système vous pourrez les enregistrer toutes d'un seul coup). Et deux autres, les principales qui permettent d'enregistrer les événements et les commandes.

**Utilisation**

Pour utiliser cette classe vous devez tout d'abord inclure les classes dans votre programme.
Ne vous inquiétez pas les classes ne s'appelles pas register_*.java je ne suis pas fou, c'est simplement intellij lors de l'export sur gist.github.com qui a  fait ça.
 
Une fois ces classes prêtes, nous allons voir comment fonctionne la classe.
Imaginons que nous voulons enregistrer toutes les classes qui implement de Listener : 

    public class Plugin extends JavaPlugin
    {
        @Override
        public void onEnable()
        {
            AutoRegister.registerEvents("fr", this);
        }
    }

Voilà c'est aussi simple que ça, le "fr" fait référence à la base de votre package mais peut aussi être quelque chose du genre "fr.app.listeners" et dans ce cas là cela va enregistrer toutes les classes dans le package "fr.app.listeners" et dans ses sous packages.
 
C'est beau non ? 
 
Se pose un problème plutôt de taille, les commandes pour les enregistrer il faut le nom de celle ci non ? 
Oui, parfaitement et j'ai pallier à ce problème avec cette interface qui est dans les sources : 

    @FunctionalInterface
    public interface CommandInfo
    {
        String getCommandName();
    }


Bonjour,
 
Introduction
 
Forcé de constater que d'enregistrer ses événements dans la classe principale du plugin devient vraiment pénible à la longue, j'ai décidé de faire une classe qui fait des enregistrements de toutes les classes d'un coup grâce à une méthode. 
Cette classe est composé de plusieurs méthodes, une pour "customiser" votre enregistrement (imaginons que vous ayez des classes qui implements de Manageable et qu'elles aient une fonction .register, grâce à mon système vous pourrez les enregistrer toutes d'un seul coup). Et deux autres, les principales qui permettent d'enregistrer les événements et les commandes.
 
Utilisation
 
Pour utiliser cette classe vous devez tout d'abord mettre ces classes dans un package : https://gist.github.com/Kwizzy-DEV/8d0fc20f31d98c0876a961216bf28e4f
Ne vous inquiétez pas les classes ne s'appelles pas register_*.java je ne suis pas fou, c'est simplement intellij lors de l'export sur gist.github.com qui a  fait ça.
 
Une fois ces classes prêtes, nous allons voir comment fonctionne la classe.
Imaginons que nous voulons enregistrer toutes les classes qui implement de Listener : 
 
public class Plugin extends JavaPlugin
{
    @Override
    public void onEnable()
    {
        AutoRegister.registerEvents("fr", this);
    }
}
Voilà c'est aussi simple que ça, le "fr" fait référence à la base de votre package mais peut aussi être quelque chose du genre "fr.app.listeners" et dans ce cas là cela va enregistrer toutes les classes dans le package "fr.app.listeners" et dans ses sous packages.
 
C'est beau non ? 
 
Se pose un problème plutôt de taille, les commandes pour les enregistrer il faut le nom de celle ci non ? 
Oui, parfaitement et j'ai pallier à ce problème avec cette interface qui est sur le gist : 
 
@FunctionalInterface
public interface CommandInfo
{
    String getCommandName();
}
Donc maintenant si vous voulez enregistrer automatiquement vos commandes il faudra que votre classe implement CommandExecutor, CommandInfo.
Vous devrez ovveride #getCommandName et donc retourner un String qui sera le nom de votre command. 
 
Ainsi vous pourrez utiliser :

    AutoRegister.registerCommands("fr", this);

Un autre problème se pose alors, si on veut utiliser des constructeurs customisés lors de l'instanciation fourni pour register comment on fait ? 
Pas de panique ! Une annotation @RegisterInstance fourni dans le gist te permet de pallier à ce problème.
Il te suffit dans ta classe de faire comme ceci (valable aussi pour les méthodes qui retournent l'instance, doivent être static et sans paramètres) : 

    class CommandExample implements CommandExecutor, CommandInfo
    {
        @RegisterInstance
        public static CommandExample instance = new CommandExample("test");
    
        String cname;
    
        public CommandExample(String name)
        {
            cname = name;
        }
    
        @Override
        public String getCommandName()
        {
            return "enjoy";
        }
    
        @Override
        public boolean onCommand(CommandSender commandSender, Command command, String s, String[] strings)
        {
            return false;
        }
    }

Et automatiquement si un field avec l'annotation @RegisterInstance est présent alors le système va prendre sa valeur, vérifier que le type est égal à la classe qui le contient et si c'est le cas alors il sera utiliser lors de l'enregistrement.
 
Bon maintenant que l'on sait comment utiliser les commandes static fourni avec la classe, on va tâter du cochon ! 

**La fonction de la mort**

Alors la fonction qui est utilisé dans la fonction (oui beaucoup de fonction ... alors que c'est des méthodes mais comme ça c'est plus claire) #registerEvents() par exemple est celle ci :
 

    public <T> void registerAction(Class<? extends T> mustBePrimary, Consumer<T> action, Class... mustBeOther)

Grâce à cette fonction vous pourrez absolument tout enregistrer ou faire des actions sur des classes hein c'est pas limiter qu'aux enregistrement de classes !
Pour l'utiliser vous devrez faire un new AutoRegister(String packageName, JavaPlugin instance).registerAction(...);
 
Explication des arguments : 
 

`Class<? extends T> mustBePrimary`, ce paramètre est très important cela veut dire : "dans toutes les classes que je vais chercher, elle doit hériter de quelle interface en premier ?"

`Consumer<T> action`, pour les débutants je vous conseil d'aller voir ce tutoriel car sinon il y a moyen que vous ne comprendrez pas. En gros je m'explique le consumer va vous donner 
T, T = `Class<? extends T> mustBePrimary` donc sera cast en mustBePrimary. Donc du coup si vous faites par exemple `t -> t -> Bukkit.getPluginManager().registerEvents(t, (un JavaPlugin)));`
Donc du coup c'est génial parceque vous pouvez faire tout ce que vous voulez avec ça :) 

`Class... mustBeOther` c'est si votre classe doit implement d'autres interfaces ;) (Utilisé dans la méthode registerCommands()
 
Voilà, faites en bonne usage :) 
