package me.rafaelauler;

public class EventManager2 {
  private Automatic2 rdmAutomatic = null;
  
  public boolean isRunningRDM() {
    return (this.rdmAutomatic != null);
  }
  
  public Automatic2 getRdmAutomatic() {
    return this.rdmAutomatic;
  }
  
  public void setRdmAutomatic(Automatic2 rdmAutomatic) {
    this.rdmAutomatic = rdmAutomatic;
  }
}