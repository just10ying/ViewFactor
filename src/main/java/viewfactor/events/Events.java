package viewfactor.events;

interface Events {
  void start();
  void finish();
  void startParseStl();
  void finishParseStl();
  void startBufferTransfer();
  void finishBufferTransfer();
  void startComputation();
  void updateComputationProgress(int current, int max);
  void finishComputation(double result);
  void info(String info);
  void exception(Exception e);
}
