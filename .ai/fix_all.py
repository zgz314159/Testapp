import os, re

base = r'C:\Users\zgz31\AndroidStudioProjects\Testapp\app\src\main\java\com\example\testapp'

def read(path):
    with open(path, 'rb') as f:
        raw = f.read()
    # Try different encodings
    for enc in ['utf-8', 'gbk', 'gb2312', 'gb18030', 'latin-1']:
        try:
            return raw.decode(enc)
        except:
            continue
    # Fallback: decode with errors replaced
    return raw.decode('utf-8', errors='replace')

def write(path, content):
    with open(path, 'w', encoding='utf-8') as f:
        f.write(content)

# HomeViewModel - remove getPracticeProgressFlowUseCase
p = os.path.join(base, 'presentation', 'screen', 'HomeViewModel.kt')
c = read(p)
c = c.replace("    private val getPracticeProgressFlowUseCase: GetPracticeProgressFlowUseCase,\n", "")
write(p, c)
print('HomeViewModel fixed')

# HomeScreen - remove wrongBookViewModel + favoriteViewModel
p = os.path.join(base, 'presentation', 'screen', 'HomeScreen.kt')
c = read(p)
c = c.replace("    private val wrongBookViewModel: WrongBookViewModel = hiltViewModel(),\n", "")
c = c.replace("    private val favoriteViewModel: FavoriteViewModel = hiltViewModel(),\n", "")
write(p, c)
print('HomeScreen fixed')

# FavoriteViewModel - remove context
p = os.path.join(base, 'presentation', 'screen', 'FavoriteViewModel.kt')
c = read(p)
c = c.replace("    private val context: Context,\n", "")
write(p, c)
print('FavoriteViewModel fixed')

# WrongBookViewModel - remove context
p = os.path.join(base, 'presentation', 'screen', 'WrongBookViewModel.kt')
c = read(p)
c = c.replace("    private val context: Context,\n", "")
write(p, c)
print('WrongBookViewModel fixed')

# BaiduAskViewModel - remove PARSE_FAILED_PREFIX
p = os.path.join(base, 'presentation', 'screen', 'BaiduAskViewModel.kt')
c = read(p)
c = c.replace("    private val PARSE_FAILED_PREFIX = ParsingConstants.PARSE_FAILED_PREFIX\n", "")
write(p, c)
print('BaiduAskViewModel fixed')

# SparkAskViewModel - remove import + PARSE_FAILED_PREFIX
p = os.path.join(base, 'presentation', 'screen', 'SparkAskViewModel.kt')
c = read(p)
c = c.replace("import com.example.testapp.presentation.screen.LocalizedResult\n", "")
c = c.replace("    private val PARSE_FAILED_PREFIX = ParsingConstants.PARSE_FAILED_PREFIX\n", "")
write(p, c)
print('SparkAskViewModel fixed')

# PracticeEditorCoordinator - remove onPracticeProgressSeed
p = os.path.join(base, 'presentation', 'screen', 'PracticeEditorCoordinator.kt')
c = read(p)
c = c.replace("    private val onPracticeProgressSeed: (Long) -> Unit,\n", "")
write(p, c)
print('PracticeEditorCoordinator fixed')

# PracticeModeCoordinator - remove saveProgressMutex + retainedById
p = os.path.join(base, 'presentation', 'screen', 'PracticeModeCoordinator.kt')
c = read(p)
c = c.replace("    private val saveProgressMutex = Mutex()\n", "")
c = c.replace("    private val retainedById = mutableMapOf<Int, Int>()\n", "")
write(p, c)
print('PracticeModeCoordinator fixed')

# PracticeNavigationCoordinator - remove rebuildQuestionWithLatestFillSettingsFn
p = os.path.join(base, 'presentation', 'screen', 'PracticeNavigationCoordinator.kt')
c = read(p)
c = c.replace("    private val rebuildQuestionWithLatestFillSettingsFn: (Int) -> Question,\n", "")
write(p, c)
print('PracticeNavigationCoordinator fixed')

# PracticeSessionCoordinator - remove setProgressId, currentMemoryRoundQuestionIds, answerHandler
p = os.path.join(base, 'presentation', 'screen', 'PracticeSessionCoordinator.kt')
c = read(p)
c = c.replace("    private val setProgressId: (String) -> Unit,\n", "")
c = c.replace("    private val currentMemoryRoundQuestionIds: () -> Set<Int>,\n", "")
c = c.replace("    private val answerHandler: PracticeAnswerHandler,\n", "")
write(p, c)
print('PracticeSessionCoordinator fixed')

# PracticeInteractionCoordinator - remove all 5 unused params
p = os.path.join(base, 'presentation', 'screen', 'PracticeInteractionCoordinator.kt')
c = read(p)
c = c.replace("    private val answerHandler: PracticeAnswerHandler,\n", "")
c = c.replace("    private val modeCoordinator: PracticeModeCoordinator,\n", "")
c = c.replace("    private val fullAnswerModeActive: () -> Boolean,\n", "")
c = c.replace("    private val fullAnswerRequireCorrect: () -> Boolean,\n", "")
c = c.replace("    private val memoryModeActive: () -> Boolean,\n", "")
write(p, c)
print('PracticeInteractionCoordinator fixed')

# PracticeSubmitCoordinator - remove answerHandler, modeCoordinator
p = os.path.join(base, 'presentation', 'screen', 'PracticeSubmitCoordinator.kt')
c = read(p)
c = c.replace("    private val answerHandler: PracticeAnswerHandler,\n", "")
c = c.replace("    private val modeCoordinator: PracticeModeCoordinator,\n", "")
write(p, c)
print('PracticeSubmitCoordinator fixed')

print('ALL DONE')
