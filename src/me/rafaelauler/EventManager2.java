package me.rafaelauler;

public class EventManager2 {
  private Automatic rdmAutomatic = null;
  
  public boolean isRunningRDM() {
    return (this.rdmAutomatic != null);
  }
  
  public Automatic getRdmAutomatic() {
    return this.rdmAutomatic;
  }
  
  public void setRdmAutomatic(Automatic rdmAutomatic) {
    this.rdmAutomatic = rdmAutomatic;
  }
}