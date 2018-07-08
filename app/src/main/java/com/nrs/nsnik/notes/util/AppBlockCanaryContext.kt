/*
 *     Notes  Copyright (C) 2018  Nikhil Soni
 *     This program comes with ABSOLUTELY NO WARRANTY; for details type `show w'.
 *     This is free software, and you are welcome to redistribute it
 *     under certain conditions; type `show c' for details.
 *
 * The hypothetical commands `show w' and `show c' should show the appropriate
 * parts of the General Public License.  Of course, your program's commands
 * might be different; for a GUI interface, you would use an "about box".
 *
 *   You should also get your employer (if you work as a programmer) or school,
 * if any, to sign a "copyright disclaimer" for the program, if necessary.
 * For more information on this, and how to apply and follow the GNU GPL, see
 * <http://www.gnu.org/licenses/>.
 *
 *   The GNU General Public License does not permit incorporating your program
 * into proprietary programs.  If your program is a subroutine library, you
 * may consider it more useful to permit linking proprietary applications with
 * the library.  If this is what you want to do, use the GNU Lesser General
 * Public License instead of this License.  But first, please read
 * <http://www.gnu.org/philosophy/why-not-lgpl.html>.
 */

package com.nrs.nsnik.notes.util


import android.content.Context
import com.github.moduth.blockcanary.BlockCanaryContext
import com.github.moduth.blockcanary.internal.BlockInfo
import java.io.File
import java.util.*

class AppBlockCanaryContext : BlockCanaryContext() {

    //TODO

    /**
     * Implement in your project.
     *
     * @return Qualifier which can specify this installation, like version + flavor.
     */
    override fun provideQualifier(): String {
        return "unknown"
    }

    /**
     * Implement in your project.
     *
     * @return user id
     */
    override fun provideUid(): String {
        return "uid"
    }

    /**
     * Network type
     *
     * @return [String] like 2G, 3G, 4G, wifi, etc.
     */
    override fun provideNetworkType(): String {
        return "unknown"
    }

    /**
     * Config monitor duration, after this time BlockCanary will stop, use
     * with `BlockCanary`'s isMonitorDurationEnd
     *
     * @return monitor last duration (in hour)
     */
    override fun provideMonitorDuration(): Int {
        return -1
    }

    /**
     * Config block threshold (in millis), dispatch over this duration is regarded as a BLOCK. You may set it
     * from performance of device.
     *
     * @return threshold in mills
     */
    override fun provideBlockThreshold(): Int {
        return 1000
    }

    /**
     * Thread stack dump interval, use when block happens, BlockCanary will dump on main thread
     * stack according to current sample cycle.
     *
     *
     * Because the implementation mechanism of Looper, real dump interval would be longer than
     * the period specified here (especially when cpu is busier).
     *
     *
     * @return dump interval (in millis)
     */
    override fun provideDumpInterval(): Int {
        return provideBlockThreshold()
    }

    /**
     * Path to save log, like "/blockcanary/", will save to sdcard if can.
     *
     * @return path of log files
     */
    override fun providePath(): String {
        return "/blockcanary/"
    }

    /**
     * If need notification to notice block.
     *
     * @return true if need, else if not need.
     */
    override fun displayNotification(): Boolean {
        return true
    }

    /**
     * Implement in your project, bundle files into a zip file.
     *
     * @param src  files before compress
     * @param dest files compressed
     * @return true if compression is successful
     */
    override fun zip(src: Array<File>, dest: File): Boolean {
        return false
    }

    /**
     * Implement in your project, bundled log files.
     *
     * @param zippedFile zipped file
     */
    override fun upload(zippedFile: File) {
        throw UnsupportedOperationException()
    }


    /**
     * Packages that developer concern, by default it uses process name,
     * put high priority one in pre-order.
     *
     * @return null if simply concern only package with process name.
     */
    override fun concernPackages(): List<String>? {
        return null
    }

    /**
     * Filter stack without any in concern package, used with @{code concernPackages}.
     *
     * @return true if filter, false it not.
     */
    override fun filterNonConcernStack(): Boolean {
        return false
    }

    /**
     * Provide white list, entry in white list will not be shown in ui list.
     *
     * @return return null if you don't need white-list filter.
     */
    override fun provideWhiteList(): List<String> {
        val whiteList = LinkedList<String>()
        whiteList.add("org.chromium")
        return whiteList
    }

    /**
     * Whether to delete files whose stack is in white list, used with white-list.
     *
     * @return true if delete, false it not.
     */
    override fun deleteFilesInWhiteList(): Boolean {
        return true
    }

    /**
     * Block interceptor, developer may provide their own actions.
     */
    override fun onBlock(context: Context, blockInfo: BlockInfo) {

    }

}
